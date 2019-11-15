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
import scalismo.ui.model.properties.{ ColorProperty, LineWidthProperty, NodeProperty, OpacityProperty }
import scalismo.ui.model.{ BoundingBox, PointCloudNode }
import scalismo.ui.rendering.actor.mixin.{ ActorColor, ActorLineWidth, ActorOpacity, ActorSceneNode }
import scalismo.ui.rendering.util.VtkUtil
import scalismo.ui.view.{ ViewportPanel, ViewportPanel2D, ViewportPanel3D }
import vtk.{ vtkGlyph3D, vtkPoints, vtkPolyData, vtkSphereSource }

object PointCloudActor extends SimpleActorsFactory[PointCloudNode] {
  override def actorsFor(renderable: PointCloudNode, viewport: ViewportPanel): Option[Actors] = {
    viewport match {
      case _: ViewportPanel3D => Some(new PointCloudActor3D(renderable))
      case _2d: ViewportPanel2D => Some(new PointCloudActor2D(renderable, _2d))
    }
  }
}

trait PointCloudActor extends SingleDataSetActor with ActorOpacity with ActorColor with ActorSceneNode {
  override def sceneNode: PointCloudNode

  override def opacity: OpacityProperty = sceneNode.opacity

  override def color: ColorProperty = sceneNode.color

  protected def onInstantiated(): Unit

  lazy val sphere = new vtkSphereSource

  private def transformedPoints: vtkPoints = new vtkPoints {
    sceneNode.transformedSource.foreach { point =>
      InsertNextPoint(point(0), point(1), point(2))
    }
  }

  lazy val polydata = new vtkPolyData

  protected lazy val glyph: vtkGlyph3D = new vtkGlyph3D {
    SetSourceConnection(sphere.GetOutputPort)
    SetInputData(polydata)
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

class PointCloudActor2D(override val sceneNode: PointCloudNode, viewport: ViewportPanel2D) extends SlicingActor(viewport) with PointCloudActor with ActorLineWidth {
  override def lineWidth: LineWidthProperty = sceneNode.lineWidth

  override protected def onSlicingPositionChanged(): Unit = rerender(false)

  override protected def onInstantiated(): Unit = {
    planeCutter.SetInputConnection(glyph.GetOutputPort())
  }

  override protected def sourceBoundingBox: BoundingBox = VtkUtil.bounds2BoundingBox(polydata.GetBounds())

}

class PointCloudActor3D(override val sceneNode: PointCloudNode) extends PointCloudActor {
  override protected def onInstantiated(): Unit = {
    mapper.SetInputConnection(glyph.GetOutputPort)
  }

}

