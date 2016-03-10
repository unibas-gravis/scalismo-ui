package scalismo.ui.rendering.actor

import vtk.vtkActor

/**
 * This is the highest-level trait in the rendering.actors package.
 * It just declares a single method that must return a list of vtkActors.
 */
trait Actors {
  def vtkActors: List[vtkActor]
}

