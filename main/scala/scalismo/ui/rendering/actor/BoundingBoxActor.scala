package scalismo.ui.rendering.actor

import java.awt.Color

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

  GetProperty().SetColor(VtkUtil.colorToArray(Color.WHITE))

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

  listenTo(slicingPosition)

  reactions += {
    case SlicingPosition.event.BoundingBoxChanged(_) => update()
    case SlicingPosition.event.VisibilityChanged(_) => update()
  }

  update()

}

class SingleBoundingBoxActor2D(override val slicingPosition: SlicingPosition, override val axis: Axis) extends SinglePolyDataActor with BoundingBoxActor2D {
  override def boundingBox: BoundingBox = BoundingBox.Invalid
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

  GetProperty().SetColor(VtkUtil.colorToArray(AxisColor.forAxis(axis)))

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
      case Axis.Y =>
        points.InsertNextPoint(bb.xMin, p.y, bb.zMin)
        points.InsertNextPoint(bb.xMax, p.y, bb.zMax)
      case Axis.Z =>
        points.InsertNextPoint(bb.xMin, bb.yMin, p.z)
        points.InsertNextPoint(bb.xMax, bb.yMax, p.z)
    }

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

  listenTo(slicingPosition)

  reactions += {
    case SlicingPosition.event.BoundingBoxChanged(_) => update()
    case SlicingPosition.event.VisibilityChanged(_) => update()
    case SlicingPosition.event.PointChanged(_, _, _) => update()
  }

  update()

}

