package scalismo.ui.rendering.internal

import java.util.concurrent.locks.ReentrantLock
import javax.media.opengl.awt.GLJPanel
import javax.media.opengl.{ GLAutoDrawable, GLCapabilities, GLEventListener, GLProfile }

import vtk._

/**
 * This is essentially a Scala re-implementation based on [[vtk.rendering.jogl.vtkJoglPanelComponent]].
 * It includes a couple of bugfixes and extensions.
 */
class RenderingComponent extends vtk.rendering.vtkComponent[GLJPanel] {

  ////// fields / constructor

  private val renderWindow = new vtkGenericOpenGLRenderWindow()
  private val lock = new ReentrantLock
  private var renderer = new vtkRenderer

  // Keep camera around to prevent its creation/deletion in Java world
  private var camera = renderer.GetActiveCamera()

  private val interceptor = new EventInterceptor()
  private val eventForwarder = new InteractorForwarder(this)
  eventForwarder.setEventInterceptor(interceptor)

  private var interactor = new RenderWindowInteractor()
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

  override def getInteractorForwarder: InteractorForwarder = eventForwarder

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

  override def getRenderWindowInteractor: RenderWindowInteractor = interactor

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
