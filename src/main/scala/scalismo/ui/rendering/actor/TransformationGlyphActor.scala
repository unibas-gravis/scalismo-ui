/*
 * Copyright (C) 2016  University of Basel, Graphics and Vision Research Group
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scalismo.ui.rendering.actor

import scalismo.ui.model.TransformationGlyphNode
import scalismo.ui.model.properties.ScalarRange
import scalismo.ui.view.{ViewportPanel, ViewportPanel2D, ViewportPanel3D}
import vtk.vtkFloatArray

object TransformationGlyphActor extends SimpleActorsFactory[TransformationGlyphNode] {
  override def actorsFor(renderable: TransformationGlyphNode, viewport: ViewportPanel): Option[Actors] = {
    viewport match {
      case _: ViewportPanel3D   => Some(new TransformationGlyphActor3D(renderable))
      case _2d: ViewportPanel2D => Some(new TransformationGlyphActor2D(renderable, _2d))
    }
  }
}

trait TransformationGlyphActor extends VectorFieldActor {

  override def sceneNode: TransformationGlyphNode

  override def rerender(geometryChanged: Boolean): Unit = {

    sceneNode.transformedSource

    val scalars = new vtkFloatArray() {
      SetNumberOfComponents(1)
    }

    val vectors = new vtkFloatArray() {
      SetNumberOfComponents(3)
    }

    var maxNorm = 0.0
    var minNorm = Double.MaxValue

    for ((vector, _) <- sceneNode.transformedSource.values.zipWithIndex) {
      val norm = vector.norm
      vectors.InsertNextTuple3(vector(0), vector(1), vector(2))
      scalars.InsertNextValue(norm)
      if (norm > maxNorm) maxNorm = norm
      if (norm < minNorm) minNorm = norm
    }

    polydata.GetPointData().SetVectors(vectors)
    polydata.GetPointData().SetScalars(scalars)

    if (geometryChanged) {
      scalarRange.value = ScalarRange(minNorm.toFloat, maxNorm.toFloat, scalarRange.value.colorMapping)
    }

    arrow.Modified()
    glyph.Update()
    glyph.Modified()
    mapper.Modified()
    actorChanged(geometryChanged)
  }

}

class TransformationGlyphActor3D(override val sceneNode: TransformationGlyphNode)
    extends VectorFieldActor3D(sceneNode)
    with TransformationGlyphActor

class TransformationGlyphActor2D(override val sceneNode: TransformationGlyphNode, viewport: ViewportPanel2D)
    extends VectorFieldActor2D(sceneNode, viewport)
    with TransformationGlyphActor
