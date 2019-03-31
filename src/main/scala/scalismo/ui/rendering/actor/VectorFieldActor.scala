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

import scalismo.ui.model.capabilities.Transformable
import scalismo.ui.model.properties._
import scalismo.ui.model.{ BoundingBox, VectorFieldNode }
import scalismo.ui.rendering.actor.mixin._
import scalismo.ui.rendering.util.VtkUtil
import scalismo.ui.view.{ ViewportPanel, ViewportPanel2D, ViewportPanel3D }
import vtk._

object VectorFieldActor extends SimpleActorsFactory[VectorFieldNode] {
  override def actorsFor(renderable: VectorFieldNode, viewport: ViewportPanel): Option[Actors] = {
    viewport match {
      case _: ViewportPanel3D => Some(new VectorFieldActor3D(renderable))
      case _2d: ViewportPanel2D => Some(new VectorFieldActor2D(renderable, _2d))
    }
  }
}

trait VectorFieldActor extends SinglePolyDataActor with ActorOpacity with ActorScalarRange with ActorSceneNode {
  override def sceneNode: VectorFieldNode

  override def opacity: OpacityProperty = sceneNode.opacity

  override def scalarRange: ScalarRangeProperty = sceneNode.scalarRange

  protected def onInstantiated(): Unit

  lazy val arrow = new vtkArrowSource

  def setupPolyData(): vtkPolyData = {

    val points = new vtkPoints
    val vectors = new vtkFloatArray() {
      SetNumberOfComponents(3)
    }

    val scalars = new vtkFloatArray() {
      SetNumberOfComponents(1)
    }

    for (((point, vector), _) <- sceneNode.source.pointsWithValues.zipWithIndex) {
      points.InsertNextPoint(point(0), point(1), point(2))
      vectors.InsertNextTuple3(vector(0), vector(1), vector(2))
      scalars.InsertNextValue(vector.norm)
    }

    new vtkPolyData {
      SetPoints(points)
      GetPointData().SetVectors(vectors)
      GetPointData().SetScalars(scalars)
    }
  }

  lazy val polydata = setupPolyData()

  lazy val glyph = new vtkGlyph3D {
    SetSourceConnection(arrow.GetOutputPort)
    SetInputData(polydata)
    //    ScalingOn()
    OrientOn()

    SetScaleModeToScaleByVector()
    SetVectorModeToUseVector()
    SetColorModeToColorByScalar()
  }

  mapper.SetInputConnection(glyph.GetOutputPort)
  mapper.ScalarVisibilityOn()

  def rerender(geometryChanged: Boolean) = {
    arrow.Modified()
    glyph.Update()
    glyph.Modified()
    mapper.Modified()
    actorChanged(geometryChanged)
  }

  listenTo(sceneNode)

  reactions += {
    case Transformable.event.GeometryChanged(_) => rerender(true)
    case NodeProperty.event.PropertyChanged(_) => rerender(false)
  }

  onInstantiated()

  rerender(true)

}

class VectorFieldActor3D(override val sceneNode: VectorFieldNode) extends VectorFieldActor {
  override protected def onInstantiated(): Unit = {
    mapper.SetInputConnection(glyph.GetOutputPort)
  }

}

class VectorFieldActor2D(override val sceneNode: VectorFieldNode, viewport: ViewportPanel2D) extends SlicingActor(viewport) with VectorFieldActor with ActorLineWidth {
  override def lineWidth: LineWidthProperty = sceneNode.lineWidth

  override protected def onSlicingPositionChanged(): Unit = rerender(false)

  override protected def onInstantiated(): Unit = {
    planeCutter.SetInputConnection(glyph.GetOutputPort())
  }

  override protected def sourceBoundingBox: BoundingBox = VtkUtil.bounds2BoundingBox(polydata.GetBounds())

}
