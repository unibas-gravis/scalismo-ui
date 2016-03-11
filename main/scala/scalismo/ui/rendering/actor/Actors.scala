package scalismo.ui.rendering.actor

import scalismo.ui.model.BoundingBox
import scalismo.ui.rendering.util.BoundingBoxUtil
import vtk.vtkActor

/**
 * This is the highest-level trait in the rendering.actors package.
 * It just declares a single method that must return a list of vtkActors.
 */
trait Actors {
  def vtkActors: List[vtkActor]

  def boundingBox: BoundingBox = {
    vtkActors.map { a => BoundingBoxUtil.bounds2BoundingBox(a.GetBounds()) }.fold(BoundingBox.Invalid)((bb1, bb2) => bb1.union(bb2))
  }
}

