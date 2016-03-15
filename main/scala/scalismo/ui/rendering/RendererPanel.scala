package scalismo.ui.rendering

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

import scalismo.ui.control.{ NodeVisibility, SlicingPosition }
import scalismo.ui.model.Scene.event.SceneChanged
import scalismo.ui.model.{ Axis, BoundingBox, Renderable }
import scalismo.ui.rendering.RendererPanel.Cameras
import scalismo.ui.rendering.actor.{ ActorEvents, Actors, ActorsFactory }
import scalismo.ui.rendering.internal.RenderingComponent
import scalismo.ui.util.EdtUtil
import scalismo.ui.view.{ ViewportPanel, ViewportPanel2D }
import vtk._

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

  val frame = viewport.frame

  private val implementation = new RenderingComponent(viewport)
  Cameras.setDefaultCameraState(implementation.getActiveCamera)

  viewport match {
    case _2d: ViewportPanel2D =>
      listenTo(viewport.frame.sceneControl.slicingPosition)
      setCameraToAxis(_2d.axis)
    case _ => // nothing
  }

  layout(Component.wrap(implementation.getComponent)) = BorderPanel.Position.Center

  listenTo(frame.scene, frame.sceneControl.nodeVisibility)

  reactions += {
    case SceneChanged(_) if attached => updateAllActors()
    case ActorEvents.event.ActorChanged(_, geometryChanged) if attached =>
      if (geometryChanged) {
        actorsChanged(cameraReset = false)
      } else {
        render()
      }
    case pc @ SlicingPosition.event.PointChanged(_, _, _) => handleSlicingPositionPointChanged(pc)
    case NodeVisibility.event.NodeVisibilityChanged(_, view) if attached && view == this.viewport => updateAllActors()
  }

  private var currentActors: List[RenderableAndActors] = Nil

  private var attached: Boolean = false

  def setAttached(attached: Boolean): Unit = {
    this.attached = attached
    updateAllActors()
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

  private var updating = false

  // this is a comparatively expensive operation, so it should only be invoked if something "big" has changed.
  private def updateAllActors(): Unit = {
    if (!updating) {
      EdtUtil.onEdtWait {
        updating = true
        val renderer = implementation.getRenderer
        val renderables = if (attached) frame.sceneControl.renderablesFor(viewport) else Nil

        val wasEmpty = currentBoundingBox == BoundingBox.Invalid

        val obsolete = currentActors.filter(ra => !renderables.exists(_ eq ra.renderable))

        val missing = renderables.diff(currentActors.map(_.renderable))
        val created = missing.map(r => new RenderableAndActors(r, ActorsFactory.factoryFor(r).flatMap(f => f.untypedActorsFor(r, viewport))))

        if (obsolete.nonEmpty) {
          obsolete.foreach(ra => ra.vtkActors.foreach { actor =>
            renderer.RemoveActor(actor)
            actor match {
              case dyn: ActorEvents =>
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
              case eventActor: ActorEvents => listenTo(eventActor)
              case _ =>
            }
            renderer.AddActor(actor)
          })
          currentActors = currentActors ++ created
        }

        if (created.nonEmpty || obsolete.nonEmpty) {
          // something has changed
          actorsChanged(cameraReset = wasEmpty)
        }
        updating = false
      }
    }
  }

  private def actorsChanged(cameraReset: Boolean): Unit = {
    currentBoundingBox = currentActors.foldLeft(BoundingBox.Invalid: BoundingBox)({
      case (bb, actors) =>
        bb.union(actors.actorsOption.map(_.boundingBox).getOrElse(BoundingBox.Invalid))
    })
    if (cameraReset) {
      resetCamera()
    } else {
      render()
    }
  }

  def resetCamera(): Unit = {
    implementation.resetCamera()
    render()
  }

  def render(): Unit = {
    implementation.render()
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

  private def handleSlicingPositionPointChanged(pc: SlicingPosition.event.PointChanged): Unit = {
    viewport match {
      case vp2d: ViewportPanel2D =>
        val cam = implementation.getActiveCamera()
        val pos = cam.GetPosition()
        val foc = cam.GetFocalPoint()
        vp2d.axis match {
          case Axis.X =>
            val amount = pc.current.x - pc.previous.x
            pos(0) += amount
            foc(0) += amount
          case Axis.Y =>
            val amount = pc.current.y - pc.previous.y
            pos(1) += amount
            foc(1) += amount
          case Axis.Z =>
            val amount = pc.current.z - pc.previous.z
            pos(2) += amount
            foc(2) += amount
        }
        cam.SetPosition(pos)
        cam.SetFocalPoint(foc)
      case _ => // can't handle
    }
  }
}
