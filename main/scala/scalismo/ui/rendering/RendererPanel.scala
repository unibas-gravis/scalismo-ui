package scalismo.ui.rendering

import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.locks.ReentrantLock
import javax.imageio.ImageIO
import javax.media.opengl.awt.GLJPanel
import javax.media.opengl.{ GLAutoDrawable, GLCapabilities, GLEventListener, GLProfile }

import scalismo.ui.model.Scene.event.SceneChanged
import scalismo.ui.control.SlicingPosition
import scalismo.ui.model.{ Axis, BoundingBox, Renderable }
import scalismo.ui.rendering.RendererPanel.Cameras
import scalismo.ui.rendering.actor.{ Actors, ActorsFactory, EventActor }
import scalismo.ui.util.EdtUtil
import scalismo.ui.view.{ ViewportPanel, ViewportPanel2D }
import vtk._
import vtk.rendering.vtkInteractorForwarder

import scala.swing.{ BorderPanel, Component }
import scala.util.Try

object RendererPanel {

  // Helper object to properly set camera positions for 2D slices.
  private[RendererPanel] object Cameras {

    // the state of a freshly created camera.
    case class DefaultCameraState(position: Array[Double], focalPoint: Array[Double], viewUp: Array[Double])

    private var _defaultCameraState: Option[DefaultCameraState] = None

    // this will have an effect exactly once, on the very first invocation.
    def setDefaultCameraState(cam: vtkCamera) = Cameras.synchronized {
      if (_defaultCameraState.isEmpty) {
        _defaultCameraState = Some(DefaultCameraState(cam.GetPosition(), cam.GetFocalPoint(), cam.GetViewUp()))
      }
    }

    def defaultCameraState: DefaultCameraState = _defaultCameraState.get

    case class CameraChangeFromDefault(pitch: Option[Double], roll: Option[Double], yaw: Option[Double])

    def cameraChangeForAxis(axis: Axis): CameraChangeFromDefault = {
      axis match {
        case Axis.Y => CameraChangeFromDefault(Some(90), None, None)
        case Axis.X => CameraChangeFromDefault(None, None, Some(90))
        case Axis.Z => CameraChangeFromDefault(None, None, None)
      }
    }
  }

}

class RendererPanel(viewport: ViewportPanel) extends BorderPanel {

  private class RenderableAndActors(val renderable: Renderable, val actorsOption: Option[Actors]) {
    def vtkActors: List[vtkActor] = actorsOption.map(_.vtkActors).getOrElse(Nil)
  }

  val scene = viewport.frame.scene

  private val implementation = new Implementation
  Cameras.setDefaultCameraState(implementation.getActiveCamera)

  viewport match {
    case _2d: ViewportPanel2D =>
      listenTo(viewport.frame.sceneControl.slicingPosition)
      setCameraToAxis(_2d.axis)
    case _ => // nothing
  }

  layout(Component.wrap(implementation.getComponent)) = BorderPanel.Position.Center

  listenTo(scene)

  reactions += {
    case SceneChanged(_) if attached => update()
    case RendererContext.event.RenderRequest(_) if attached && !updating => implementation.Render()
    case pc @ SlicingPosition.event.PointChanged(_, _, _) => handlePointChanged(pc)
  }

  private var currentActors: List[RenderableAndActors] = Nil

  private var attached: Boolean = false
  private var updating = false

  def setAttached(attached: Boolean): Unit = {
    this.attached = attached
    update()
  }

  private var _currentBoundingBox: BoundingBox = BoundingBox.Invalid

  def currentBoundingBox: BoundingBox = _currentBoundingBox

  private def currentBoundingBox_=(newBb: BoundingBox): Unit = {
    if (_currentBoundingBox != newBb) {
      _currentBoundingBox = newBb
      // we take a shortcut here, and directly send on behalf of the viewport.
      viewport.publishEvent(ViewportPanel.event.BoundingBoxChanged(viewport))
    }
  }

  def update(): Unit = EdtUtil.onEdtWait {
    updating = true
    val renderer = implementation.getRenderer
    val renderables = if (attached) scene.renderables else Nil

    val wasEmpty = currentActors.isEmpty

    val obsolete = currentActors.filter(ra => !renderables.exists(_ eq ra.renderable))

    val missing = renderables.diff(currentActors.map(_.renderable))
    val created = missing.map(r => new RenderableAndActors(r, ActorsFactory.factoryFor(r).flatMap(f => f.untypedActorsFor(r, viewport))))

    if (obsolete.nonEmpty) {
      obsolete.foreach(ra => ra.vtkActors.foreach { actor =>
        renderer.RemoveActor(actor)
        actor match {
          case dyn: EventActor =>
            deafTo(dyn)
            dyn.onDestroy()
          case _ => // do nothing
        }
      })
      currentActors = currentActors diff obsolete
    }

    if (created.nonEmpty) {
      created.foreach(_.vtkActors.foreach { actor =>
        actor match {
          case dyn: EventActor => listenTo(dyn)
          case _ =>
        }
        renderer.AddActor(actor)
      })
      currentActors = currentActors ++ created
    }

    if (created.nonEmpty || obsolete.nonEmpty) {
      // something has changed
      actorsChanged(camReset = wasEmpty)
    }
    updating = false
  }

  private def actorsChanged(camReset: Boolean): Unit = {
    if (camReset) {
      resetCamera()
    }
    currentBoundingBox = currentActors.foldLeft(BoundingBox.Invalid: BoundingBox)({
      case (bb, actors) =>
        bb.union(actors.actorsOption.map(_.boundingBox).getOrElse(BoundingBox.Invalid))
    })
  }

  def resetCamera(): Unit = {
    implementation.resetCamera()
  }

  def screenshot(file: File): Try[Unit] = Try {
    val source = implementation.getComponent
    val image = new BufferedImage(source.getWidth, source.getHeight, BufferedImage.TYPE_INT_RGB)
    val g = image.createGraphics()

    // parameter description: see
    // https://jogamp.org/deployment/jogamp-next/javadoc/jogl/javadoc/javax/media/opengl/awt/GLJPanel.html#setupPrint%28double,%20double,%20int,%20int,%20int%29
    source.setupPrint(1, 1, 0, -1, -1)
    source.printAll(g)
    source.releasePrint()

    image.flush()
    ImageIO.write(image, "png", file)
  }

  def setCameraToAxis(axis: Axis): Unit = {
    val cam = implementation.getActiveCamera
    val default = Cameras.defaultCameraState
    cam.SetPosition(default.position)
    cam.SetFocalPoint(default.focalPoint)
    cam.SetViewUp(default.viewUp)

    val change = Cameras.cameraChangeForAxis(axis)
    change.yaw.foreach(v => cam.Azimuth(v))
    change.pitch.foreach(v => cam.Elevation(v))
    change.roll.foreach(v => cam.Roll(v))
    cam.OrthogonalizeViewUp()
    resetCamera()
  }

  private def handlePointChanged(pc: SlicingPosition.event.PointChanged): Unit = {
    viewport match {
      case vp2d: ViewportPanel2D =>
        val cam = implementation.getActiveCamera()
        val pos = cam.GetPosition()
        val foc = cam.GetFocalPoint()
        vp2d.axis match {
          case Axis.X =>
            val amount = pc.previous.x - pc.current.x
            pos(0) += amount
            foc(0) += amount
          case Axis.Y =>
            val amount = pc.previous.y - pc.current.y
            pos(1) += amount
            foc(1) += amount
          case Axis.Z =>
            val amount = pc.previous.z - pc.current.z
            pos(2) += amount
            foc(2) += amount
        }
        cam.SetPosition(pos)
        cam.SetFocalPoint(foc)
      case _ => // can't handle
    }
  }

  /**
   * This is essentially a Scala re-implementation based on [[vtk.rendering.jogl.vtkJoglPanelComponent]].
   * It includes a couple of bugfixes and extensions.
   */
  private class Implementation extends vtk.rendering.vtkComponent[GLJPanel] {

    ////// fields / constructor

    private val renderWindow = new vtkGenericOpenGLRenderWindow()
    private val lock = new ReentrantLock
    private var renderer = new vtkRenderer

    // Keep camera around to prevent its creation/deletion in Java world
    private var camera = renderer.GetActiveCamera()

    private val eventForwarder = new vtkInteractorForwarder(this)
    // FIXME
    private var interactor = new vtkGenericRenderWindowInteractor()
    private val glPanel = new GLJPanel(new GLCapabilities(GLProfile.getDefault()))

    private var inRenderCall = false

    // Initialize interactor
    interactor.SetRenderWindow(renderWindow)
    interactor.TimerEventResetsTimerOff()
    interactor.ConfigureEvent()
    interactor.SetInteractorStyle(new vtkInteractorStyleTrackballCamera)

    // Setup event forwarder
    interactor.AddObserver("CreateTimerEvent", eventForwarder, "StartTimer")
    interactor.AddObserver("DestroyTimerEvent", eventForwarder, "DestroyTimer")

    // Link renderWindow with renderer
    renderWindow.AddRenderer(renderer)

    renderWindow.SetIsDirect(1)
    renderWindow.SetSupportsOpenGL(1)
    renderWindow.SetIsCurrent(true)

    // Make sure that when VTK internally requests a Render, it gets triggered properly
    renderWindow.AddObserver("WindowFrameEvent", this, "Render")
    renderWindow.GetInteractor.AddObserver("RenderEvent", this, "Render")
    renderWindow.GetInteractor.SetEnableRender(false)

    // Bind interactor forwarder
    glPanel.addMouseListener(eventForwarder)
    glPanel.addMouseMotionListener(eventForwarder)
    glPanel.addKeyListener(eventForwarder)

    glPanel.addGLEventListener(new GLEventListener {

      override def init(drawable: GLAutoDrawable): Unit = {
        // Make sure the JOGL context is current
        val ctx = drawable.getContext
        if (!ctx.isCurrent) {
          ctx.makeCurrent()
        }

        // Init VTK OpenGL RenderWindow

        // renderWindow.SetMapped(1) // this is there in the original code, but causes DEADLOCKs on Windows.
        renderWindow.SetPosition(0, 0)
        renderWindow.OpenGLInit()

        setSize(drawable.getSurfaceWidth, drawable.getSurfaceHeight)

      }

      override def reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int): Unit = {
        setSize(width, height)
      }

      override def display(drawable: GLAutoDrawable): Unit = {
        inRenderCall = true
        renderWindow.Render()
        inRenderCall = false
      }

      override def dispose(drawable: GLAutoDrawable): Unit = {
        Delete()
        vtkObjectBase.JAVA_OBJECT_MANAGER.gc(false)
      }
    })

    ////// helper methods

    private def executeLocked(block: => Unit) = {
      lock.lock()
      block
      lock.unlock()
    }

    private def executeInterruptibly(block: => Unit) = {
      try {
        lock.lockInterruptibly()
        block
      } catch {
        case e: InterruptedException => // nothing we can do
      } finally {
        lock.unlock()
      }
    }

    ////// overrides

    override def Delete(): Unit = executeLocked {
      renderer = null
      camera = null
      interactor = null
      /* ORIGINAL COMMENT FROM vtk.rendering.vtkAbstractComponent.java:
       * removing the renderWindow is let to the superclass
       * because in the very special case of an AWT component
       * under Linux, destroying renderWindow crashes.
       */
    }

    override def getActiveCamera: vtkCamera = camera

    override def getRenderWindow: vtkRenderWindow = renderWindow

    override def getInteractorForwarder: vtkInteractorForwarder = eventForwarder

    override def resetCamera(): Unit = {
      if (renderer != null) {
        executeInterruptibly {
          renderer.ResetCamera()
        }
      }
      Render()
    }

    override def getRenderer: vtkRenderer = renderer

    override def getVTKLock: ReentrantLock = lock

    override def getRenderWindowInteractor: vtkGenericRenderWindowInteractor = interactor

    override def getComponent: GLJPanel = glPanel

    override def resetCameraClippingRange(): Unit = {
      if (renderer != null) {
        executeInterruptibly(renderer.ResetCameraClippingRange())
      }
    }

    override def setInteractorStyle(style: vtkInteractorStyle): Unit = {
      if (interactor != null) {
        executeLocked(interactor.SetInteractorStyle(style))
      }
    }

    override def setSize(w: Int, h: Int): Unit = {
      if (renderWindow != null && interactor != null) {
        executeInterruptibly {
          renderWindow.SetSize(w, h)
          interactor.SetSize(w, h)
        }
      }
    }

    override def Render(): Unit = {
      if (!inRenderCall) {
        glPanel.repaint()
      }
    }

  }

}
