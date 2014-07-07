package org.statismo.stk.ui.vtk

import vtk.{vtkPoints, vtkOutlineFilter, vtkPolyData}
import org.statismo.stk.ui.{BoundingBox, Scene}

class BoundingBoxActor3D(source: Scene.SlicingPosition.BoundingBoxRenderable3D) extends PolyDataActor {
  val scene = source.source.scene

  this.GetProperty().SetColor(1, 1, 1)
  listenTo(scene)
  update(withEvent = false)

  reactions += {
    case Scene.SlicingPosition.BoundingBoxChanged(s) => update()
  }

  def update(withEvent: Boolean = true) = this.synchronized {
    val points = new vtkPoints()
    val bb = source.source.boundingBox
    points.InsertNextPoint(bb.xMin, bb.yMin, bb.zMin)
    points.InsertNextPoint(bb.xMax, bb.yMax, bb.zMax)

    val poly = new vtkPolyData()
    poly.SetPoints(points)

    val outline = new vtkOutlineFilter()
    outline.SetInputData(poly)
    mapper.SetInputConnection(outline.GetOutputPort())
    mapper.Modified()

    outline.Delete()
    poly.Delete()
    points.Delete()

    if (withEvent) {
      publishEdt(VtkContext.RenderRequest(this))
    }
  }

  override def onDestroy() = this.synchronized {
    deafTo(scene)
    super.onDestroy()
  }

  // this actor is showing the bounding box, but doesn't "have" one itself (i.e., doesn't affect it).
  override lazy val currentBoundingBox = BoundingBox.None
}