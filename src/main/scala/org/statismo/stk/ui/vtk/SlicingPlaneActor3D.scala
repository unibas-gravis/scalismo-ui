package org.statismo.stk.ui.vtk

import org.statismo.stk.ui.{TwoDViewport, Axis, BoundingBox, Scene}
import vtk.vtkPoints

class SlicingPlaneActor(source: Scene.SlicingPosition, axis: Axis.Value)(implicit vtkViewport: VtkViewport) extends PolyDataActor {
  val scene = source.scene

  listenTo(scene)
  update(withEvent = false)

  reactions += {
    case Scene.SlicingPosition.BoundingBoxChanged(s) => update()
    case Scene.SlicingPosition.PointChanged(s) => update()
  }

  def update(withEvent: Boolean = true) = this.synchronized {
    // FIXME: this is essentially a quick hack for now, because we're "abusing" the bounding box functionality.

    val bb = source.boundingBox
    val p = source.point

    val points = new vtkPoints()

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

    val poly = new vtk.vtkPolyData()
    poly.SetPoints(points)

    val outline = new vtk.vtkOutlineFilter()
    outline.SetInputData(poly)
    mapper.RemoveAllInputs()
    mapper.SetInputConnection(outline.GetOutputPort())
    mapper.Modified()

    outline.Delete()
    poly.Delete()
    points.Delete()

    // this should actually only be set once, but for some reason, the lines sometimes return to black (???)
    axis match {
      case Axis.X => GetProperty().SetColor(1, 0, 0)
      case Axis.Y => GetProperty().SetColor(0, 1, 0)
      case Axis.Z => GetProperty().SetColor(0, 0, 1)
    }

    //GetProperty().SetColor(0, 0, 0)

    if (withEvent) {
      publishEdt(VtkContext.RenderRequest(this))
    }
  }

  override def onDestroy() = this.synchronized {
    deafTo(scene)
    super.onDestroy()
  }
}

class SlicingPlaneActor3D(plane: Scene.SlicingPosition.SlicingPlaneRenderable3D)(implicit vtkViewport: VtkViewport) extends SlicingPlaneActor(plane.source, plane.axis) {
  override lazy val currentBoundingBox = BoundingBox.None
}

class SlicingPlaneActor2D(plane: Scene.SlicingPosition.SlicingPlaneRenderable2D)(implicit vtkViewport: VtkViewport) extends SlicingPlaneActor(plane.source, vtkViewport.viewport.asInstanceOf[TwoDViewport].axis) {
  override lazy val currentBoundingBox = BoundingBox.None
}