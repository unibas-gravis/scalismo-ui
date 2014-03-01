package org.statismo.stk.ui.vtk

import scala.swing.event.Event

import org.statismo.stk.core.geometry.Point3D
import org.statismo.stk.ui.EdtPublisher
import org.statismo.stk.ui.Viewport
import org.statismo.stk.ui.Workspace

import vtk.vtkCellPicker
import vtk.vtkGenericRenderWindowInteractor

object VtkRenderWindowInteractor {
  case class PointClicked(point: Point3D) extends Event
}

class VtkRenderWindowInteractor(workspace: Workspace, var viewport: Viewport) extends vtkGenericRenderWindowInteractor with EdtPublisher {
  val cellPicker = new vtkCellPicker
  SetPicker(cellPicker)

  private var height = 0
  private var x: Int = 0
  private var y: Int = 0

  private var downX: Int = 0
  private var downY: Int = 0

  override def SetEventInformationFlipY(x: Int, y: Int, ctrl: Int, shift: Int, unk1: Char, unk2: Int, unk3: String) = {
    this.x = x
    this.y = y
    super.SetEventInformationFlipY(x, y, ctrl, shift, unk1, unk2, unk3)
  }

  def renderer = GetRenderWindow().GetRenderers().GetFirstRenderer()

  override def LeftButtonPressEvent() = {
    if (workspace.landmarkClickMode) {
      downX = x
      downY = y
    }
    if (viewport.isMouseSensitive) {
    	super.LeftButtonPressEvent()
    }
  }

  override def SetSize(width: Int, height: Int) = {
    this.height = height
    super.SetSize(width, height)
  }

  override def LeftButtonReleaseEvent() = {
    if (viewport.isMouseSensitive) {
    	super.LeftButtonReleaseEvent()
    }

    if (workspace.landmarkClickMode) {
      val threshold = 3 //(pixels)
      if (Math.abs(x - downX) < threshold && Math.abs(y - downY) < threshold) {
        val p = cellPicker.Pick(x, height - y - 1, 0.0, renderer)
        if (p == 1) {
          cellPicker.PickFromListOff()
          val pickpos = cellPicker.GetPickPosition()
          val prop = cellPicker.GetProp3D()
          if (prop != null) {
            prop match {
              case clickable: ClickableActor =>
                clickable.clicked(Point3D(pickpos(0).toFloat, pickpos(1).toFloat, pickpos(2).toFloat))
              case _: DisplayableActor =>
              // do nothing. We found one of our own actors, but it doesn't react to clicks
              case _ =>
                // we found an actor, but it's none of our own (probably one from an image plane). Since we don't know how to handle this ourselves,
                // we publish an event instead
                publish(VtkRenderWindowInteractor.PointClicked(Point3D(pickpos(0).toFloat, pickpos(1).toFloat, pickpos(2).toFloat)))
            }
          }
        }
      }
    }
  }
  
  override def MiddleButtonPressEvent() = {
    if (viewport.isMouseSensitive) {
    	super.MiddleButtonPressEvent()
    }
  }
  
  override def MiddleButtonReleaseEvent() = {
    if (viewport.isMouseSensitive) {
    	super.MiddleButtonReleaseEvent()
    }
  }
  
  override def RightButtonPressEvent() = {
    if (viewport.isMouseSensitive) {
    	super.RightButtonPressEvent()
    }
  }
  
  override def RightButtonReleaseEvent() = {
    if (viewport.isMouseSensitive) {
    	super.RightButtonReleaseEvent()
    }
  }
}