package org.statismo.stk.ui.vtk

import org.statismo.stk.ui.Displayable
import org.statismo.stk.ui.Mesh
import org.statismo.stk.ui.SphereLike
import org.statismo.stk.ui.ThreeDImagePlane
import org.statismo.stk.core.common.ScalarValue

import vtk.vtkActor

object DisplayableActor {
  def apply(displayable: Displayable)(implicit interactor: VtkRenderWindowInteractor): Option[DisplayableActor] = {
    displayable match {
      case m: Mesh => Some(new MeshActor(m))
      case s: SphereLike => Some(new SphereActor(s))
      case i: ThreeDImagePlane[_] => Some(new ImagePlaneActor(i))
      case _ =>
        println("DisplayableActor: Dunno what to do with " + displayable.getClass())
        None
    }
  }
  
}

trait DisplayableActor extends VtkContext {
  def vtkActors: Seq[vtkActor]
  def onDestroy(): Unit = ()
}