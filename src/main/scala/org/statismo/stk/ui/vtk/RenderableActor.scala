package org.statismo.stk.ui.vtk


import vtk.vtkActor
import org.statismo.stk.ui.visualization.{SphereLike, Renderable}
import org.statismo.stk.ui.Mesh.ThreeDMeshRenderable
import org.statismo.stk.ui.Image3D
import scala.None

object RenderableActor {
  type RenderableToActor = (Renderable, VtkRenderWindowInteractor) => Option[RenderableActor]
  def apply(renderable: Renderable)(implicit interactor: VtkRenderWindowInteractor): Option[RenderableActor] = {
    // first, use the function in case the user overwrote something
    val raOption = renderableToActorFunction(renderable, interactor)
    if (raOption.isDefined) raOption
    else {
      renderable match {
        case r: VtkRenderable => Some(r.getVtkActor)
        case _ =>
          println("RenderableActor: Dunno what to do with " + renderable.getClass)
          None
      }
    }
  }

  val DefaultRenderableToActorFunction: RenderableToActor = { case (renderable, interactor) =>
    implicit val _interactor = interactor
    renderable match {
      case m: ThreeDMeshRenderable => Some(new MeshActor(m))
      case s: SphereLike => Some(new SphereActor(s))
      case i: Image3D.Renderable3D => Some(new ImageWidgetActor(i))
      case _ => None
    }
  }

  private var _renderableToActorFunction = DefaultRenderableToActorFunction
  def renderableToActorFunction = this.synchronized(_renderableToActorFunction)
  def renderableToActorFunction(nf: RenderableToActor) = this.synchronized {
    _renderableToActorFunction = nf
  }
}

trait RenderableActor extends VtkContext {
  def vtkActors: Seq[vtkActor]

  def onDestroy(): Unit = ()
}