package org.statismo.stk.ui.vtk

import vtk.vtkActor
import org.statismo.stk.ui.Displayable
import org.statismo.stk.ui.Mesh
import org.statismo.stk.ui.Sphere
import org.statismo.stk.ui.SphereLike

object DisplayableActor {
  def apply(displayable: Displayable): Option[DisplayableActor] = {
    displayable match {
      case _: Mesh => {
        Some(new MeshActor(displayable.asInstanceOf[Mesh]))
      }
      case _: SphereLike => {
        Some(new SphereActor(displayable.asInstanceOf[SphereLike]))
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
}