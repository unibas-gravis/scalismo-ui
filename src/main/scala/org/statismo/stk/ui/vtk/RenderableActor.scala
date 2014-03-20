package org.statismo.stk.ui.vtk

import org.statismo.stk.ui.Displayable
import org.statismo.stk.ui.Mesh
import org.statismo.stk.ui.SphereLike
import org.statismo.stk.ui.ThreeDImagePlane

import vtk.vtkActor
import org.statismo.stk.ui.visualization.Renderable
import org.statismo.stk.ui.Mesh.MeshRenderable

object RenderableActor {
  def apply(renderable: Renderable)(implicit interactor: VtkRenderWindowInteractor): Option[RenderableActor] = {
    renderable match {
      case m: MeshRenderable => Some(new MeshActor(m))
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