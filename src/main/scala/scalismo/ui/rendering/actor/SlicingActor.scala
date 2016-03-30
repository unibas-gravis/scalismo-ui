package scalismo.ui.rendering.actor

import scalismo.ui.control.SlicingPosition
import scalismo.ui.model.{ Axis, BoundingBox }
import scalismo.ui.view.ViewportPanel2D
import vtk.{ vtkCutter, vtkPlane }

abstract class SlicingActor(val viewport: ViewportPanel2D) extends SinglePolyDataActor with ActorEvents {

  private def slicingPositionChanged(slicingPosition: SlicingPosition): Unit = {
    val newValue = (viewport.axis match {
      case Axis.X => slicingPosition.x
      case Axis.Y => slicingPosition.y
      case Axis.Z => slicingPosition.z
    }).toDouble

    if (planeCutter.GetValue(0) != newValue) {
      planeCutter.SetValue(0, newValue)
      onSlicingPositionChanged()
    }
  }

  // hook for subclasses
  protected def onSlicingPositionChanged(): Unit

  // make sure to return the bounding box of the object you're slicing through.
  protected def sourceBoundingBox: BoundingBox

  final override def boundingBox: BoundingBox = sourceBoundingBox

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

  GetProperty().SetLighting(false)
  SetBackfaceProperty(GetProperty())

  listenTo(viewport.frame.sceneControl.slicingPosition)

  reactions += {
    case SlicingPosition.event.PointChanged(s, _, _) => slicingPositionChanged(s)
  }

  slicingPositionChanged(viewport.frame.sceneControl.slicingPosition)

}
