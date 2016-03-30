package scalismo.ui.rendering.actor

import scalismo.ui.model.BoundingBox
import scalismo.ui.rendering.util.VtkUtil
import vtk.vtkActor

/**
 * This is the highest-level trait in the rendering.actors package.
 */
trait Actors {
  def vtkActors: List[vtkActor]

  def boundingBox: BoundingBox = {
    vtkActors.map { a => VtkUtil.bounds2BoundingBox(a.GetBounds()) }.fold(BoundingBox.Invalid)((bb1, bb2) => bb1.union(bb2))
  }
}

