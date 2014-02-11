package org.statismo.stk.ui.vtk

import vtk.vtkActor
import org.statismo.stk.ui.Displayable
import org.statismo.stk.ui.Mesh
import org.statismo.stk.ui.Sphere
import org.statismo.stk.ui.SphereLike
import org.statismo.stk.ui.ThreeDImagePlane

object DisplayableActor {
  def apply(displayable: Displayable)(implicit interactor: VtkRenderWindowInteractor): Option[DisplayableActor] = {
    displayable match {
      case _: Mesh => {
        Some(new MeshActor(displayable.asInstanceOf[Mesh]))
      }
      case _: SphereLike => {
        Some(new SphereActor(displayable.asInstanceOf[SphereLike]))
      }
      case _: ThreeDImagePlane => {
        Some(new ImagePlaneActor(displayable.asInstanceOf[ThreeDImagePlane]))
      }
      case _ => {
        println("DisplayableActor: Dunno what to do with " + displayable.getClass())
        None
      }
    }
  }
}

trait DisplayableActor extends VtkContext {
  def vtkActors: Seq[vtkActor]
  def onDestroy(): Unit = ()
}