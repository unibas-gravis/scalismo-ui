package org.statismo.stk.ui.vtk

import scala.collection.immutable.HashMap
import scala.swing.event.Event

import org.statismo.stk.ui.{BoundingBox, EdtPublisher, Scene, Viewport}

import vtk.vtkRenderer
import org.statismo.stk.ui.visualization.{Renderable, Visualizable}
import scala.util.{Success, Failure}

trait VtkContext extends EdtPublisher

object VtkContext {

  case class ResetCameraRequest(source: VtkContext) extends Event

  case class RenderRequest(source: VtkContext) extends Event

  case class ViewportEmptyStatus(source: VtkViewport, isEmpty: Boolean) extends Event

}

class VtkViewport(val viewport: Viewport, val renderer: vtkRenderer, val interactor: VtkRenderWindowInteractor) extends VtkContext {
  val scene = viewport.scene
  private implicit val myself: VtkViewport  = this
  deafTo(this)

  private var actors = new HashMap[Renderable, Option[RenderableActor]]

  private var firstTime = true

  def refresh(): Unit = {
    val renderables = scene.visualizables(f => f.isVisibleIn(viewport)).flatMap {obj =>
      scene.visualizations.tryGet(obj, viewport) match {
        case Failure(f) => {
          f.printStackTrace()
          Nil
        }
        case Success(vis) => vis(obj)
      }
    }
    refresh(renderables)
  }
  def refresh(backend: Seq[Renderable]): Unit = /*Swing.onEDT*/ {
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
      if (changed) {
        updateBoundingBox()
        if (viewport.currentBoundingBox eq BoundingBox.None) {
          // this is a safe way of determining whether there are any "real" actors.
          // "helper" actors (like the BoundingBox itself) are not taken into account
          // while calculating the boundingbox.
          publish(VtkContext.ViewportEmptyStatus(this, true))
        } else {
          if (firstTime) {
            firstTime = false
            val camMod = viewport.initialCameraChange
            val cam = renderer.GetActiveCamera()
            camMod.yaw.map(v => cam.Azimuth(v))
            camMod.pitch.map(v => cam.Elevation(v))
            camMod.roll.map(v => cam.Roll(v))
            cam.OrthogonalizeViewUp()
            resetCamera()
          } else {
            publish(VtkContext.RenderRequest(this))
          }
        }
      }
    }
  }

  def updateBoundingBox() = {
    val boundingBox = actors.values.foldLeft(BoundingBox.None)({case (bb, a) => bb.union(a.map(_.currentBoundingBox).orElse(Some(BoundingBox.None)).get)})
    viewport.currentBoundingBox = boundingBox
  }
  listenTo(scene, viewport)

  reactions += {
    case Viewport.Destroyed(v) => destroy()
    case Scene.TreeTopologyChanged(s) => refresh()
    case Scene.VisibilityChanged(s) => refresh()
    case VtkContext.ResetCameraRequest(s) => publish(VtkContext.ResetCameraRequest(this))
    case VtkContext.RenderRequest(s) => {
      updateBoundingBox()
      publish(VtkContext.RenderRequest(this))
    }
  }

  refresh()

  def destroy() = this.synchronized {
    deafTo(scene, viewport)
    refresh(Nil)
  }

  def resetCamera() = {
    renderer.ResetCamera()
    publish(VtkContext.RenderRequest(this))
  }
}