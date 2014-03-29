package org.statismo.stk.ui.vtk

import org.statismo.stk.ui.{Axis, BoundingBox, Scene}
import org.statismo.stk.core.geometry.Point3D
import scala.collection.immutable

class SlicingPlaneActor3D(plane: Scene.SlicingPosition.SlicingPlaneRenderable3D) extends PolyDataActor {
  val scene = plane.source.scene

  this.GetProperty().SetColor(1, 0, 0)
  listenTo(scene)
  update(false)

  reactions += {
    case Scene.SlicingPosition.BoundingBoxChanged(s) => update()
    case Scene.SlicingPosition.PointChanged(s) => update()
  }

  def update(withEvent: Boolean = true) = this.synchronized {
    // FIXME: this is essentially a quick hack for now, because we're "abusing" the bounding box functionality.

    val points = new vtk.vtkPoints()
    val bb = plane.source.boundingBox
    val p = plane.source.point

    plane.axis match {
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

    val poly = new vtk.vtkPolyData()
    poly.SetPoints(points)

    val outline = new vtk.vtkOutlineFilter()
    outline.SetInputData(poly)
    mapper.SetInputConnection(outline.GetOutputPort())
    mapper.Modified()

    outline.Delete()
    poly.Delete()
    points.Delete()

    if (withEvent) {
      publish(VtkContext.RenderRequest(this))
    }
  }

  override def onDestroy() = this.synchronized {
    deafTo(scene)
    super.onDestroy()
  }

  override lazy val currentBoundingBox = BoundingBox.None
}