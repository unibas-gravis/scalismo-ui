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

import scalismo.common.UnstructuredPointsDomain
import scalismo.geometry._3D
import scalismo.mesh._
import scalismo.ui.model.capabilities.Transformable
import scalismo.ui.model.properties._
import scalismo.ui.model.{ BoundingBox, ScalarFieldNode }
import scalismo.ui.rendering.actor.mixin._
import scalismo.ui.rendering.util.VtkUtil
import scalismo.ui.view.{ ViewportPanel, ViewportPanel2D, ViewportPanel3D }
import scalismo.utils.MeshConversion
import vtk.{ vtkGlyph3D, vtkPoints, vtkPolyData, vtkSphereSource }

object ScalarFieldActor extends SimpleActorsFactory[ScalarFieldNode] {
  override def actorsFor(renderable: ScalarFieldNode, viewport: ViewportPanel): Option[Actors] = {
    viewport match {
      case _: ViewportPanel3D => Some(new ScalarFieldActor3D(renderable))
      case _2d: ViewportPanel2D => Some(new ScalarFieldActor2D(renderable, _2d))
    }
  }
}

trait ScalarFieldActor extends SinglePolyDataActor with ActorOpacity with ActorScalarRange with ActorSceneNode {
  override def sceneNode: ScalarFieldNode

  override def opacity: OpacityProperty = sceneNode.opacity

  def scalarRange: ScalarRangeProperty = sceneNode.scalarRange

  protected def onInstantiated(): Unit

  lazy val sphere = new vtkSphereSource

  def transformedPoints: vtkPoints = new vtkPoints {
    sceneNode.transformedSource.domain.points.foreach { point =>
      InsertNextPoint(point(0), point(1), point(2))
    }
  }

  protected lazy val polydata: vtkPolyData = {
    // Hack alert! We create a triangle mesh with empty cells and built from it a scalarMeshData.
    // In this way we can use the conversion utilities and have colors for free

    val meshDomain = UnstructuredPointsDomain[_3D](sceneNode.source.domain.points.toIndexedSeq)
    val mesh = TriangleMesh3D(meshDomain, TriangleList(IndexedSeq[TriangleCell]()))
    val smf = ScalarMeshField(mesh, sceneNode.source.data)
    MeshConversion.scalarMeshFieldToVtkPolyData(smf)
  }

  protected lazy val glyph: vtkGlyph3D = new vtkGlyph3D {
    SetSourceConnection(sphere.GetOutputPort)
    SetInputData(polydata)
    SetScaleModeToDataScalingOff()
  }

  def rerender(geometryChanged: Boolean): Unit = {

    if (geometryChanged) {
      polydata.SetPoints(transformedPoints)
    }
    sphere.SetRadius(sceneNode.radius.value)
    mapper.Modified()
    actorChanged(geometryChanged)
  }

  listenTo(sceneNode, sceneNode.radius)

  reactions += {
    case Transformable.event.GeometryChanged(_) => rerender(true)
    case NodeProperty.event.PropertyChanged(p) if p eq sceneNode.radius => rerender(true)
  }

  onInstantiated()

  rerender(true)

}

class ScalarFieldActor2D(override val sceneNode: ScalarFieldNode, viewport: ViewportPanel2D) extends SlicingActor(viewport) with ScalarFieldActor with ActorLineWidth {
  override def lineWidth: LineWidthProperty = sceneNode.lineWidth

  override protected def onSlicingPositionChanged(): Unit = rerender(false)

  override protected def onInstantiated(): Unit = {
    planeCutter.SetInputConnection(glyph.GetOutputPort())
  }

  override protected def sourceBoundingBox: BoundingBox = VtkUtil.bounds2BoundingBox(polydata.GetBounds())

}

class ScalarFieldActor3D(override val sceneNode: ScalarFieldNode) extends ScalarFieldActor {
  override protected def onInstantiated(): Unit = {
    mapper.SetInputConnection(glyph.GetOutputPort)
  }

}
