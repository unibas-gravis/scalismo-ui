package scalismo.ui.rendering.actor

import scalismo.ui.model.properties.NodeProperty.event.PropertyChanged
import scalismo.ui.model.properties.{ScalarRangeProperty, ScalarRange}
import scalismo.ui.model.{VectorFieldNode, TransformationGlyphNode}
import scalismo.ui.view.{ViewportPanel2D, ViewportPanel3D, ViewportPanel}
import vtk.vtkFloatArray

object TransformationGlyphActor extends SimpleActorsFactory[TransformationGlyphNode] {
  override def actorsFor(renderable: TransformationGlyphNode, viewport: ViewportPanel): Option[Actors] = {
    viewport match {
      case _3d: ViewportPanel3D => Some(new TransformationGlyphActor3D(renderable))
      case _2d: ViewportPanel2D => Some(new TransformationGlyphActor2D(renderable, _2d))
    }
  }
}

trait TransformationGlyphActor extends VectorFieldActor {

  override def sceneNode: TransformationGlyphNode

  override def rerender(geometryChanged: Boolean) = {

    sceneNode.transformedSource

    val scalars = new vtkFloatArray() {
      SetNumberOfComponents(1)
    }

    val vectors = new vtkFloatArray() {
      SetNumberOfComponents(3)
    }

    var maxNorm = 0.0;
    var minNorm = Double.MaxValue

    for ((vector, i) <- sceneNode.transformedSource.values.zipWithIndex) {
      val norm = vector.norm
        vectors.InsertNextTuple3(vector(0), vector(1), vector(2))
        scalars.InsertNextValue(norm)
      if (norm > maxNorm) maxNorm = norm
      if (norm < minNorm) minNorm = norm
    }

    polydata.GetPointData().SetVectors(vectors)
    polydata.GetPointData().SetScalars(scalars)


    if (geometryChanged) {
      scalarRange.value = ScalarRange(minNorm.toFloat, maxNorm.toFloat, minNorm.toFloat, maxNorm.toFloat)
      publishEvent(PropertyChanged(scalarRange))
    }

    arrow.Modified()
    glyph.Update()
    glyph.Modified()
    mapper.Modified()
    actorChanged(geometryChanged)
  }


}

class TransformationGlyphActor3D(override val sceneNode: TransformationGlyphNode) extends  VectorFieldActor3D(sceneNode) with TransformationGlyphActor
class TransformationGlyphActor2D(override val sceneNode: TransformationGlyphNode, viewport: ViewportPanel2D) extends VectorFieldActor2D(sceneNode, viewport) with TransformationGlyphActor