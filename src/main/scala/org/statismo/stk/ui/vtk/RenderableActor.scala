package org.statismo.stk.ui.vtk


import vtk.{vtkRenderer, vtkActor}
import org.statismo.stk.ui.visualization.{SphereLike, Renderable}
import org.statismo.stk.ui.Mesh.{MeshRenderable2DOutline, MeshRenderable3D}
import org.statismo.stk.ui.{Scene, BoundingBox, Image3D}
import scala.None

object RenderableActor {
  type RenderableToActor = (Renderable, VtkViewport) => Option[RenderableActor]
  def apply(renderable: Renderable)(implicit vtkViewport: VtkViewport): Option[RenderableActor] = {
    // first, use the function in case the user overwrote something
    val raOption = renderableToActorFunction(renderable, vtkViewport)
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
      case bb: Scene.SlicingPosition.BoundingBoxRenderable3D => Some(new BoundingBoxActor3D(bb))
      case m: MeshRenderable3D => Some(new MeshActor3D(m))
      case m: MeshRenderable2DOutline => Some(new MeshActor2DOutline(m))
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
  def currentBoundingBox: BoundingBox
  def onDestroy(): Unit = ()
}