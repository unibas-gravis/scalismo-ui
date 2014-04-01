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
      case bb3d: Scene.SlicingPosition.BoundingBoxRenderable3D => Some(new BoundingBoxActor3D(bb3d))
      case sp3d: Scene.SlicingPosition.SlicingPlaneRenderable3D => Some(new SlicingPlaneActor3D(sp3d))
      case sp2d: Scene.SlicingPosition.SlicingPlaneRenderable2D => Some(new SlicingPlaneActor2D(sp2d))
      case m3d: MeshRenderable3D => Some(new MeshActor3D(m3d))
      case m2d: MeshRenderable2DOutline => Some(new MeshActor2DOutline(m2d))
      case img3d: Image3D.Renderable3D => Some(new ImageActor3D(img3d.source))
      case img2d: Image3D.Renderable2D => Some(ImageActor2D(img2d.source))
      case s: SphereLike => Some(new SphereActor(s))
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