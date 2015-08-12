package scalismo.ui.vtk

import _root_.vtk.{ vtkActor, vtkCamera, vtkRenderer }
import scalismo.ui._
import scalismo.ui.util.EdtUtil
import scalismo.ui.visualization.Renderable

import scala.collection.immutable.HashMap
import scala.swing.event.Event

trait VtkContext extends EdtPublisher

object VtkContext {

  case class ResetCameraRequest(source: VtkContext) extends Event

  case class MoveCameraRequest(source: VtkContext, axis: Axis.Value, amount: Double) extends Event

  case class RenderRequest(source: VtkContext, immediately: Boolean = false) extends Event

}

object VtkViewport {

  private[VtkViewport] case class InitialCameraState(position: Array[Double], focalPoint: Array[Double], viewUp: Array[Double])

  private var _initialCameraState: Option[InitialCameraState] = None

  def initCameraState(cam: vtkCamera): InitialCameraState = this.synchronized {
    _initialCameraState.getOrElse {
      val state: InitialCameraState = InitialCameraState(cam.GetPosition(), cam.GetFocalPoint(), cam.GetViewUp())
      _initialCameraState = Some(state)
      state
    }
  }

  case class InitialCameraChange(pitch: Option[Double], roll: Option[Double], yaw: Option[Double])

  val NoInitialCameraChange = InitialCameraChange(None, None, None)

  def initialCameraChangeForAxis(axis: Option[Axis.Value]): InitialCameraChange = {
    axis match {
      case Some(Axis.Y) => InitialCameraChange(Some(90), None, None)
      case Some(Axis.X) => InitialCameraChange(None, None, Some(90))
      case Some(Axis.Z) => NoInitialCameraChange
      case _ => NoInitialCameraChange
    }
  }
}

class VtkViewport(val parent: VtkPanel, val renderer: vtkRenderer) extends VtkContext {
  private implicit val myself: VtkViewport = this
  deafTo(myself)

  private var actors = new HashMap[Renderable, Option[RenderableActor]]

  private var firstTime = true

  def refresh(scene: Scene): Unit = {
    val renderables = parent.viewportOption match {
      case Some(viewport) =>
        scene.visualizables {
          f => f.isVisibleIn(viewport)
        }.flatMap { o => o.visualizationStrategy.applyUntyped(o, viewport)
        }
      case _ => Nil
    }
    refresh(renderables, parent.viewportOption)
  }

  def refresh(backend: Seq[Renderable], viewportOption: Option[Viewport]): Unit = EdtUtil.onEdt {
    //println("refresh: "+backend.length)
    this.synchronized {
      var changed = false
      // remove obsolete actors
      actors.filterNot({
        case (back, front) => backend.exists({
          _ eq back
        })
      }).foreach({
        case (back, front) =>
          actors -= back
          if (front.isDefined) {
            deafTo(front.get)
            front.get.vtkActors.foreach({
              a =>
                renderer.RemoveActor(a)
            })
            front.get.onDestroy()
            changed = true
          }
      })

      // determine new actors
      val toCreate = backend.filterNot {
        d =>
          actors.keys.exists {
            k => k eq d
          }
      }

      val created = toCreate.map { d => Tuple2(d, RenderableActor(d)) }

      created.foreach({
        case (back, front) =>
          actors += Tuple2(back, front)
          if (front.isDefined) {
            listenTo(front.get)
            changed = true
            front.get.vtkActors.foreach({
              a =>
                renderer.AddActor(a)
            })
          }
      })
      // if needed, re-order the renderer's actors so that image actors come before all others. This prevents subtle drawing bugs (outlines sometimes disappearing)
      if (changed) {
        val actors = renderer.GetActors()
        val count = actors.GetNumberOfItems()
        if (count > 1) {
          actors.InitTraversal()
          val original = (0 until count) map { i => actors.GetNextActor() }

          // we need to add images first, so that shape outlines always get drawn after images
          def imagesFirst(a1: vtkActor, a2: vtkActor): Boolean = {
            (a1, a2) match {
              case (i1: ImageActor2D, i2: ImageActor2D) => false
              case (i1: ImageActor2D, _) => true
              case (_, _) => false
            }
          }

          val sorted = original sortWith imagesFirst
          if (sorted != original) {
            original foreach renderer.RemoveActor
            sorted foreach renderer.AddActor
          }
        }
      }
      if (changed || firstTime) {
        viewportOption match {
          case Some(viewport) =>
            updateBoundingBox()

            // the if statement below should be a safe way of determining whether there are any "real" actors.
            // "helper" actors (like the BoundingBox itself) are not taken into account
            // while calculating the boundingbox.
            if (viewport.currentBoundingBox ne BoundingBox.None) {
              if (firstTime) {
                firstTime = false

                val axisOption = viewport match {
                  case vp2d: TwoDViewport => Some(vp2d.axis)
                  case _ => None
                }

                setCameraToAxis(axisOption)

              } else {
                publishEdt(VtkContext.RenderRequest(this))
              }
            } else {
              // no bounding box (i.e., empty viewport)
              if (changed && !firstTime) {
                firstTime = true
                publishEdt(VtkContext.RenderRequest(this))
              }
            }
          case _ =>
        }
      }
    }
  }

  def updateBoundingBox() = {
    val boundingBox = actors.values.foldLeft(BoundingBox.None)({
      case (bb, a) => bb.union(a.map(_.currentBoundingBox).orElse(Some(BoundingBox.None)).get)
    })
    parent.viewportOption.foreach(_.currentBoundingBox = boundingBox)
  }

  listenTo(parent)

  reactions += {

    case Scene.TreeTopologyChanged(s) => refresh(s)
    case Scene.VisibilityChanged(s) => refresh(s)
    case Scene.PerspectiveChangeCompleted(s) => refresh(s)

    case VtkContext.ResetCameraRequest(s) => publishEdt(VtkContext.ResetCameraRequest(this))
    case VtkContext.RenderRequest(s, now) =>
      updateBoundingBox()
      publishEdt(VtkContext.RenderRequest(this, now))
    case VtkContext.MoveCameraRequest(src, axis, amount) => moveCamera(axis, amount)
  }

  def attach() = this.synchronized {
    val vp = parent.viewportOption.get
    listenTo(vp, vp.scene)
  }

  def detach() = this.synchronized {
    firstTime = true
    parent.viewportOption match {
      case Some(viewport) =>
        deafTo(viewport, viewport.scene)
        refresh(Nil, Some(viewport))
      case _ =>
    }
  }

  def moveCamera(axis: Axis.Value, amount: Double) = EdtUtil.onEdt {
    val pos = renderer.GetActiveCamera().GetPosition()
    axis match {
      case Axis.X => pos(0) += amount
      case Axis.Y => pos(1) += amount
      case Axis.Z => pos(2) += amount
    }
    renderer.GetActiveCamera().SetPosition(pos)
  }

  def resetCamera(force: Boolean = false) = EdtUtil.onEdt {
    renderer.ResetCamera()
    publishEdt(VtkContext.RenderRequest(this, force))
  }

  def setCameraToAxis(axis: Axis.Value): Unit = setCameraToAxis(Some(axis))

  private def setCameraToAxis(axis: Option[Axis.Value]): Unit = EdtUtil.onEdt {
    val cam = renderer.GetActiveCamera()
    val init = VtkViewport.initCameraState(cam)
    cam.SetPosition(init.position)
    cam.SetFocalPoint(init.focalPoint)
    cam.SetViewUp(init.viewUp)

    val camMod = VtkViewport.initialCameraChangeForAxis(axis)
    camMod.yaw.foreach(v => cam.Azimuth(v))
    camMod.pitch.foreach(v => cam.Elevation(v))
    camMod.roll.foreach(v => cam.Roll(v))
    cam.OrthogonalizeViewUp()
    resetCamera(force = true)
  }

  def viewport: Viewport = parent.viewportOption.get
}
