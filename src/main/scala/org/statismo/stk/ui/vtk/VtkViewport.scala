package org.statismo.stk.ui.vtk

import scala.collection.immutable.HashMap
import scala.swing.event.Event

import org.statismo.stk.ui.{BoundingBox, EdtPublisher, Scene, Viewport}

import vtk.{vtkCamera, vtkRenderer}
import org.statismo.stk.ui.visualization.{Renderable, Visualizable}
import scala.util.{Success, Failure}

trait VtkContext extends EdtPublisher

object VtkContext {
  case class ResetCameraRequest(source: VtkContext) extends Event
  case class RenderRequest(source: VtkContext, immediately: Boolean=false) extends Event
  case class ViewportEmptyStatus(source: VtkViewport, isEmpty: Boolean) extends Event
}

object VtkViewport {
  private [VtkViewport] case class InitialCameraState(position: Array[Double], focalPoint: Array[Double], viewUp: Array[Double])

  private var _initialCameraState: Option[InitialCameraState] = None

  def initCameraState(cam: vtkCamera): InitialCameraState = this.synchronized {
    _initialCameraState.getOrElse {
      val state: InitialCameraState = InitialCameraState(cam.GetPosition(), cam.GetFocalPoint(), cam.GetViewUp())
      _initialCameraState = Some(state)
      state
    }
  }
}

class VtkViewport(val parent: VtkPanel, val renderer: vtkRenderer, val interactor: VtkRenderWindowInteractor) extends VtkContext {
  private implicit val myself: VtkViewport  = this
  deafTo(myself)

  private var actors = new HashMap[Renderable, Option[RenderableActor]]

  private var firstTime = true

  def refresh(scene: Scene): Unit = {
    val renderables = parent.viewportOption match {
      case Some(viewport) =>
        scene.visualizables(f => f.isVisibleIn(viewport)).flatMap {obj =>
        scene.visualizations.tryGet(obj, viewport) match {
          case Failure(f) =>
            f.printStackTrace()
            Nil
          case Success(vis) => vis(obj)
        }
      }
      case _ => Nil
    }
    //FIXME
    //refresh(Nil, parent.viewportOption)
    refresh(renderables, parent.viewportOption)
  }
  def refresh(backend: Seq[Renderable], viewportOption: Option[Viewport]): Unit = /*Swing.onEDT*/ {
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

      val created = toCreate.map(d => Tuple2(d, RenderableActor(d)))
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
      if (changed || firstTime) {
        viewportOption match {
          case Some(viewport) =>
            updateBoundingBox()
            if (viewport.currentBoundingBox eq BoundingBox.None) {
              // this is a safe way of determining whether there are any "real" actors.
              // "helper" actors (like the BoundingBox itself) are not taken into account
              // while calculating the boundingbox.
              publish(VtkContext.ViewportEmptyStatus(this, isEmpty = true))
            } else {
              if (firstTime) {
                firstTime = false

                val camMod = viewport.initialCameraChange
                val cam = renderer.GetActiveCamera()

                val init = VtkViewport.initCameraState(cam)
                cam.SetPosition(init.position)
                cam.SetFocalPoint(init.focalPoint)
                cam.SetViewUp(init.viewUp)

                camMod.yaw.map(v => cam.Azimuth(v))
                camMod.pitch.map(v => cam.Elevation(v))
                camMod.roll.map(v => cam.Roll(v))
                cam.OrthogonalizeViewUp()
                resetCamera(true)
              } else {
                publish(VtkContext.RenderRequest(this))
              }
            }
          case _ =>
        }
      }
    }
  }

  def updateBoundingBox() = {
    val boundingBox = actors.values.foldLeft(BoundingBox.None)({case (bb, a) => bb.union(a.map(_.currentBoundingBox).orElse(Some(BoundingBox.None)).get)})
    parent.viewportOption.map(_.currentBoundingBox = boundingBox)
  }
  listenTo(parent)

  reactions += {

    case Scene.TreeTopologyChanged(s) => refresh(s)
    case Scene.VisibilityChanged(s) => refresh(s)
    case VtkContext.ResetCameraRequest(s) => publish(VtkContext.ResetCameraRequest(this))
    case VtkContext.RenderRequest(s, now) =>
      updateBoundingBox()
      publish(VtkContext.RenderRequest(this, now))
  }

  def attach() = this.synchronized {
    val vp = parent.viewportOption.get
    listenTo(vp, vp.scene)
    refresh(vp.scene)
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

  def resetCamera(force: Boolean = false ) = {
    renderer.ResetCamera()
    publish(VtkContext.RenderRequest(this, force))
  }

  def viewport: Viewport = parent.viewportOption.get
}