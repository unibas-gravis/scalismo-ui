package org.statismo.stk.ui.vtk

import org.statismo.stk.core.geometry.{_3D, Point}

import scala.swing.event.Event

import org.statismo.stk.ui.EdtPublisher
import org.statismo.stk.ui.Viewport
import org.statismo.stk.ui.Workspace

import vtk.vtkCellPicker
import vtk.vtkGenericRenderWindowInteractor
import java.awt.{Point => APoint}

object VtkRenderWindowInteractor {

  case class PointClicked(point: Point[_3D]) extends Event

}

class VtkRenderWindowInteractor(parent: VtkPanel) extends vtkGenericRenderWindowInteractor with EdtPublisher {
  val cellPicker = new vtkCellPicker
  SetPicker(cellPicker)

  private var height = 0
  private var currentPt = new APoint
  private var lastPt = new APoint

  override def SetEventInformationFlipY(x: Int, y: Int, ctrl: Int, shift: Int, unk1: Char, unk2: Int, unk3: String) = {
    currentPt = new APoint(x, y)
    super.SetEventInformationFlipY(x, y, ctrl, shift, unk1, unk2, unk3)
  }

  protected[vtk] def workspaceOption: Option[Workspace] = parent.workspaceOption

  protected[vtk] def viewportOption: Option[Viewport] = parent.viewportOption

  def renderer = GetRenderWindow().GetRenderers().GetFirstRenderer()

  override def LeftButtonPressEvent() = {
    (workspaceOption, viewportOption) match {
      case (Some(workspace), Some(viewport)) =>
        if (workspace.landmarkClickMode) {
          lastPt = new APoint(currentPt.x, currentPt.y)
        }
        //FIXME: this is ugly
        val ok = viewport.onLeftButtonDown(currentPt)
        if (ok || workspace.landmarkClickMode) {
          super.LeftButtonPressEvent()
          if (!ok) {
            super.LeftButtonReleaseEvent()
          }
        }
      case _ =>
    }
  }

  override def SetSize(width: Int, height: Int) = {
    this.height = height
    super.SetSize(width, height)
  }

  override def LeftButtonReleaseEvent() = {
    (workspaceOption, viewportOption) match {
      case (Some(workspace), Some(viewport)) =>
        if (viewport.onLeftButtonUp(currentPt)) {
          super.LeftButtonReleaseEvent()
        }

        if (workspace.landmarkClickMode) {
          val threshold = 3 //(pixels)
          if (Math.abs(currentPt.x - lastPt.x) < threshold && Math.abs(currentPt.y - lastPt.y) < threshold) {
            val p = cellPicker.Pick(currentPt.x, height - currentPt.y - 1, 0.0, renderer)
            if (p == 1) {
              cellPicker.PickFromListOff()
              val pickpos = cellPicker.GetPickPosition()
              val prop = cellPicker.GetProp3D()
              if (prop != null) {
                prop match {
                  case clickable: ClickableActor =>
                    clickable.clicked(Point(pickpos(0).toFloat, pickpos(1).toFloat, pickpos(2).toFloat))
                  case _: RenderableActor =>
                  // do nothing. We found one of our own actors, but it doesn't react to clicks
                  case _ =>
                    // we found an actor, but it's none of our own (probably one from an image plane). Since we don't know how to handle this ourselves,
                    // we publish an event instead
                    publishEdt(VtkRenderWindowInteractor.PointClicked(Point(pickpos(0).toFloat, pickpos(1).toFloat, pickpos(2).toFloat)))
                }
              }
            }
          }
        }
      case _ =>
    }
  }

  override def MiddleButtonPressEvent() = {
    viewportOption match {
      case Some(viewport) =>
        if (viewport.onMiddleButtonDown(currentPt)) {
          super.MiddleButtonPressEvent()
        }
      case _ =>
    }
  }

  override def MiddleButtonReleaseEvent() = {
    viewportOption match {
      case Some(viewport) =>
        if (viewport.onMiddleButtonUp(currentPt)) {
          super.MiddleButtonReleaseEvent()
        }
      case _ =>
    }
  }

  override def RightButtonPressEvent() = {
    viewportOption match {
      case Some(viewport) =>
        if (viewport.onRightButtonDown(currentPt)) {
          super.RightButtonPressEvent()
        }
      case _ =>
    }
  }

  override def RightButtonReleaseEvent() = {
    viewportOption match {
      case Some(viewport) =>
        if (viewport.onRightButtonUp(currentPt)) {
          super.RightButtonReleaseEvent()
        }
      case _ =>
    }
  }
}