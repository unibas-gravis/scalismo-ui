package org.statismo.stk.ui.vtk


import vtk.vtkActor
import org.statismo.stk.ui.visualization.{SphereLike, Renderable}
import org.statismo.stk.ui.Mesh.ThreeDMeshRenderable

object RenderableActor {
  def apply(renderable: Renderable)(implicit interactor: VtkRenderWindowInteractor): Option[RenderableActor] = {
    renderable match {
      case m: ThreeDMeshRenderable => Some(new MeshActor(m))
      case s: SphereLike => Some(new SphereActor(s))
//      case s: SphereLike => Some(new SphereActor(s))
//      case i: ThreeDImagePlane[_] => Some(new ImagePlaneActor(i))
      case _ =>
        println("DisplayableActor: Dunno what to do with " + renderable.getClass)
        None
    }
  }

}

trait RenderableActor extends VtkContext {
  def vtkActors: Seq[vtkActor]

  def onDestroy(): Unit = ()
}