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

import java.awt.Color

import scalismo.geometry.Point3D
import scalismo.ui.control.SlicingPosition
import scalismo.ui.control.SlicingPosition.renderable.BoundingBoxRenderable
import scalismo.ui.model.{ Axis, BoundingBox }
import scalismo.ui.rendering.util.VtkUtil
import scalismo.ui.view.util.AxisColor
import scalismo.ui.view.{ ViewportPanel, ViewportPanel2D, ViewportPanel3D }
import vtk._

object BoundingBoxActor extends SimpleActorsFactory[SlicingPosition.renderable.BoundingBoxRenderable] {
  override def actorsFor(renderable: BoundingBoxRenderable, viewport: ViewportPanel): Option[Actors] = {
    viewport match {
      case _3d: ViewportPanel3D => Some(new BoundingBoxActor3D(renderable.source))
      case _2d: ViewportPanel2D => Some(new SingleBoundingBoxActor2D(renderable.source, _2d.axis))
    }
  }
}

class BoundingBoxActor3D(slicingPosition: SlicingPosition) extends PolyDataActor with Actors with ActorEvents {
  // this actor is displaying the bounding box, but doesn't "have" one itself (i.e., doesn't affect it).
  override def boundingBox: BoundingBox = BoundingBox.Invalid

  val sliceActors = Axis.All.map(axis => BoundingBoxActor2D(axis, slicingPosition))

  override def vtkActors: List[vtkActor] = this :: sliceActors

  def update() = {
    val points = new vtkPoints()
    val bb = slicingPosition.boundingBox
    points.InsertNextPoint(bb.xMin, bb.yMin, bb.zMin)
    points.InsertNextPoint(bb.xMax, bb.yMax, bb.zMax)

    val poly = new vtkPolyData()
    poly.SetPoints(points)

    val outline = new vtkOutlineFilter()
    outline.SetInputData(poly)
    mapper.SetInputConnection(outline.GetOutputPort())
    mapper.Modified()

    val visible = bb != BoundingBox.Invalid && slicingPosition.visible
    GetProperty().SetOpacity(if (visible) 1 else 0)

    actorChanged()
  }

  GetProperty().SetColor(VtkUtil.colorToArray(Color.WHITE))

  // this actor is not pickable (for clicking etc.)
  SetPickable(0)

  listenTo(slicingPosition)

  reactions += {
    case SlicingPosition.event.BoundingBoxChanged(_) => update()
    case SlicingPosition.event.VisibilityChanged(_) => update()
  }

  update()

}

class SingleBoundingBoxActor2D(override val slicingPosition: SlicingPosition, override val axis: Axis) extends BoundingBoxActor2D with Actors {
  override def boundingBox: BoundingBox = BoundingBox.Invalid

  override lazy val intersectionActors = {
    Axis.All.filterNot(_ == axis).map { iAxis =>
      new BoundingBoxIntersectionActor(iAxis)
    }
  }

  override val vtkActors: List[vtkActor] = this :: intersectionActors
}

object BoundingBoxActor2D {
  def apply(axis0: Axis, slicingPosition0: SlicingPosition): BoundingBoxActor2D = new BoundingBoxActor2D {
    override def axis: Axis = axis0

    override def slicingPosition: SlicingPosition = slicingPosition0
  }
}

trait BoundingBoxActor2D extends PolyDataActor with ActorEvents {

  def slicingPosition: SlicingPosition

  def axis: Axis

  def intersectionActors: List[BoundingBoxIntersectionActor] = Nil

  def update() = {
    // this is a (minimally) more expensive way to construct what will end up being
    // a rectangle anyway, but it's extremely concise and much more understandable than manual construction.

    val points = new vtkPoints()
    val bb = slicingPosition.boundingBox
    val p = slicingPosition.point

    axis match {
      case Axis.X =>
        points.InsertNextPoint(p.x, bb.yMin, bb.zMin)
        points.InsertNextPoint(p.x, bb.yMax, bb.zMax)
        intersectionActors.foreach(_.update(bb, p, 0, p.x))
      case Axis.Y =>
        points.InsertNextPoint(bb.xMin, p.y, bb.zMin)
        points.InsertNextPoint(bb.xMax, p.y, bb.zMax)
        intersectionActors.foreach(_.update(bb, p, 1, p.y))
      case Axis.Z =>
        points.InsertNextPoint(bb.xMin, bb.yMin, p.z)
        points.InsertNextPoint(bb.xMax, bb.yMax, p.z)
        intersectionActors.foreach(_.update(bb, p, 2, p.z))
    }

    val poly = new vtkPolyData()
    poly.SetPoints(points)

    val outline = new vtkOutlineFilter()
    outline.SetInputData(poly)

    mapper.SetInputConnection(outline.GetOutputPort())
    mapper.Modified()

    val visible = bb != BoundingBox.Invalid && slicingPosition.visible

    (this :: intersectionActors).foreach { a =>
      a.GetProperty().SetOpacity(if (visible) 1 else 0)
    }

    actorChanged()
  }

  GetProperty().SetColor(VtkUtil.colorToArray(AxisColor.forAxis(axis)))

  // this actor is not pickable (for clicking etc.)
  SetPickable(0)

  listenTo(slicingPosition)

  reactions += {
    case SlicingPosition.event.BoundingBoxChanged(_) => update()
    case SlicingPosition.event.VisibilityChanged(_) => update()
    case SlicingPosition.event.PointChanged(_, _, _) => update()
  }

  update()

}

// this class draws the intersection line for a particular axis in a BoundingBoxActor2D
class BoundingBoxIntersectionActor(axis: Axis) extends PolyDataActor {
  def update(bb: BoundingBox, point: Point3D, overrideIndex: Int, overrideValue: Double) = {
    val min = Array(bb.xMin, bb.yMin, bb.zMin)
    val max = Array(bb.xMax, bb.yMax, bb.zMax)

    // nail our own axis
    axis match {
      case Axis.X =>
        min(0) = point.x
        max(0) = point.x
      case Axis.Y =>
        min(1) = point.y
        max(1) = point.y
      case Axis.Z =>
        min(2) = point.z
        max(2) = point.z
    }

    // nail whichever axis our parent provided
    min(overrideIndex) = overrideValue
    max(overrideIndex) = overrideValue

    // now we have a line with only one degree of freedom
    // left, which is what we want.

    val points = new vtkPoints()
    points.InsertNextPoint(min)
    points.InsertNextPoint(max)

    val line = new vtkLine()
    line.GetPointIds().SetId(0, 0)
    line.GetPointIds().SetId(1, 1)

    val lines = new vtkCellArray()
    lines.InsertNextCell(line)

    val poly = new vtkPolyData()
    poly.SetPoints(points)
    poly.SetLines(lines)

    mapper.SetInputData(poly)
    mapper.Update()

  }

  GetProperty().SetColor(VtkUtil.colorToArray(AxisColor.forAxis(axis)))

  // this actor is not pickable (for clicking etc.)
  SetPickable(0)

}

