package org.statismo.stk.ui.vtk

import scala.collection.immutable.HashMap
import scala.swing.event.Event

import org.statismo.stk.ui.Displayable
import org.statismo.stk.ui.EdtPublisher
import org.statismo.stk.ui.Scene
import org.statismo.stk.ui.Viewport

import vtk.vtkRenderer

trait VtkContext extends EdtPublisher

object VtkContext {
  case class RenderRequest(source: VtkContext) extends Event
  case class ViewportEmpty(source: VtkViewport) extends Event
}

class VtkViewport(val viewport: Viewport, val renderer: vtkRenderer, implicit val interactor: VtkRenderWindowInteractor) extends VtkContext {
  val scene = viewport.scene
  deafTo(this)
  
  private var actors = new HashMap[Displayable, Option[DisplayableActor]]

  private var firstTime = true
  def refresh(backend: List[Displayable]) = /*Swing.onEDT*/ {
    this.synchronized {
      var changed = false

      // remove obsolete actors
      actors.filterNot({ case (back, front) => backend.exists({ _ eq back }) }).foreach({
        case (back, front) => {
          actors -= back
          if (front.isDefined) {
            deafTo(front.get)
            front.get.vtkActors.foreach({ a =>
              renderer.RemoveActor(a)
              changed = true
            })
            front.get.onDestroy()
          }
        }
      })

      // determine new actors
      val toCreate = backend.filterNot { d =>
        actors.keys.exists { k => k eq d }
      }

      val created = toCreate.map(d => Tuple2(d, DisplayableActor(d)))
      created.foreach({
        case (back, front) =>
          actors += Tuple2(back, front)
          if (front.isDefined) {
            listenTo(front.get)
            changed = true
            front.get.vtkActors.foreach({ a =>
              renderer.AddActor(a)
            })
          }
      })
      if (changed) {
        if (actors.isEmpty) {
          publish(VtkContext.ViewportEmpty(this))
        } else {
          if (firstTime) {
            firstTime = false
            resetCamera()
          } else {
            publish(VtkContext.RenderRequest(this))
          }
        }
      }
    }
  }

  listenTo(scene, viewport)
  
  reactions += {
    case Viewport.Destroyed(v) => destroy()
    case Scene.TreeTopologyChanged(s) => refresh(scene.displayables.filter(_.isShownInViewport(viewport)))
    case Scene.VisibilityChanged(s) => refresh(scene.displayables.filter(_.isShownInViewport(viewport)))
    case VtkContext.RenderRequest(s) => publish(VtkContext.RenderRequest(this))
  }
  
  refresh(scene.displayables.filter(_.isShownInViewport(viewport)))

  def destroy() = this.synchronized {
    deafTo(scene, viewport)
    refresh(Nil)
  }

  def resetCamera() = {
    renderer.ResetCamera()
    publish(VtkContext.RenderRequest(this))
  }
}