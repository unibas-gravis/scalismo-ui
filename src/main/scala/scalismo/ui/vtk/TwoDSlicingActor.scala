package scalismo.ui.vtk

import scalismo.ui.{ Axis, BoundingBox, Scene, TwoDViewport }
import vtk.{ vtkCutter, vtkPlane }

abstract class TwoDSlicingActor(val viewport: TwoDViewport) extends SinglePolyDataActor {

  protected val plane = new vtkPlane()
  plane.SetOrigin(0, 0, 0)

  viewport.axis match {
    case Axis.X => plane.SetNormal(1, 0, 0)
    case Axis.Y => plane.SetNormal(0, 1, 0)
    case Axis.Z => plane.SetNormal(0, 0, 1)
  }

  protected val planeCutter = new vtkCutter()

  planeCutter.SetCutFunction(plane)
  planeCutter.SetValue(0, 0)

  mapper.SetInputConnection(planeCutter.GetOutputPort())

  this.GetProperty().SetLighting(false)
  this.SetBackfaceProperty(this.GetProperty())

  listenTo(viewport.scene)

  slicePositionChanged(viewport.scene.slicingPosition)

  reactions += {
    case Scene.SlicingPosition.PointChanged(s, _, _) => slicePositionChanged(s)
  }

  private def slicePositionChanged(slicingPosition: Scene.SlicingPosition): Unit = {
    val newValue = (viewport.axis match {
      case Axis.X => slicingPosition.x
      case Axis.Y => slicingPosition.y
      case Axis.Z => slicingPosition.z
    }).toDouble

    if (planeCutter.GetValue(0) != newValue) {
      planeCutter.SetValue(0, newValue)
      onSlicePositionChanged()
    }
  }

  protected def onSlicePositionChanged(): Unit

  override def onDestroy() = this.synchronized {
    deafTo(viewport.scene)
    super.onDestroy()
  }

  // make sure to return the bounding box of the object you're slicing through.
  protected def sourceBoundingBox: BoundingBox

  final override def currentBoundingBox: BoundingBox = sourceBoundingBox
}
