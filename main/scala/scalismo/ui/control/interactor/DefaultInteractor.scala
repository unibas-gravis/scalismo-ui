package scalismo.ui.control.interactor

import java.awt.event.{ MouseEvent, MouseWheelEvent }
import java.awt.{ Color, Cursor }

import scalismo.ui.control.interactor.Interactor.Result
import scalismo.ui.control.interactor.Interactor.Result.{ Block, Pass }
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.view.{ ScalismoFrame, ViewportPanel2D }

import scala.swing.ToggleButton
import scala.swing.event.ButtonClicked

class DefaultInteractor extends Interactor {
  val landmarkingButton = new ToggleButton {
    val myIcon = BundledIcon.Landmark

    def updateUi() = {
      val onOff = if (selected) "ON" else "OFF"
      tooltip = s"Toggle landmarking (currently $onOff)"
      val iconColor = if (selected) Color.GREEN.darker else Color.DARK_GRAY
      icon = myIcon.colored(iconColor).standardSized()
    }

    reactions += {
      case ButtonClicked(_) => updateUi()
    }

    updateUi()
  }

  override def onActivated(frame: ScalismoFrame): Unit = {
    frame.toolBar.add(landmarkingButton)
  }

  override def onDeactivated(frame: ScalismoFrame): Unit = {
    frame.toolBar.remove(landmarkingButton)
  }

  override def mousePressed(e: MouseEvent): Result = {
    e.viewport match {
      case _2d: ViewportPanel2D if e.getButton != MouseEvent.BUTTON3 => Block
      case _ => Pass
    }
  }

  override def mouseReleased(e: MouseEvent): Result = {
    e.viewport match {
      case _2d: ViewportPanel2D if e.getButton != MouseEvent.BUTTON3 => Block
      case _ => Pass
    }
  }

  override def mouseClicked(e: MouseEvent): Result = {
    if (landmarkingButton.selected) {
      // TODO: handle landmarking
    }
    super.mouseClicked(e)
  }

  // set the cursor to a crosshair if we're in landmarking mode
  override def mouseEntered(e: MouseEvent): Result = {
    val cursor = if (landmarkingButton.selected) Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) else Cursor.getDefaultCursor
    e.canvas.setCursor(cursor)
    Pass
  }

  // allow scrolling through 2D viewports. We simply trigger the existing +/- buttons.
  override def mouseWheelMoved(e: MouseWheelEvent): Unit = {
    e.viewport match {
      case _2d: ViewportPanel2D =>
        val button = if (e.getWheelRotation > 0) _2d.positionMinusButton else _2d.positionPlusButton
        button.action.apply()
      case _ =>
    }
  }
}
