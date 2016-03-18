package scalismo.ui.control.interactor

import java.awt.event.{ MouseEvent, MouseWheelEvent }
import java.awt.{ Color, Cursor }

import scalismo.ui.control.interactor.Interactor.Verdict
import scalismo.ui.control.interactor.Interactor.Verdict.Pass
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.view.ScalismoFrame

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
    frame.toolbar.add(landmarkingButton)
  }

  override def onDeactivated(frame: ScalismoFrame): Unit = {
    frame.toolbar.remove(landmarkingButton)
  }

  override def mousePressed(e: MouseEvent): Verdict = {
    Recipe.Block2DTranslationAndRotation.mousePressed(e)
  }

  override def mouseReleased(e: MouseEvent): Verdict = {
    Recipe.Block2DTranslationAndRotation.mouseReleased(e)
  }

  override def mouseClicked(e: MouseEvent): Verdict = {

    if (landmarkingButton.selected) {
      Recipe.AddLandmarkOnClick.mouseClicked(e)
    } else {
      Pass
    }
  }

  override def mouseMoved(e: MouseEvent): Verdict = {
    if (landmarkingButton.selected) {
      Recipe.HighlightOutlineOfPickableObject.mouseMoved(e)
    } else {
      Pass
    }
  }

  // set the cursor to a crosshair if we're in landmarking mode
  override def mouseEntered(e: MouseEvent): Verdict = {
    val cursor = if (landmarkingButton.selected) Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) else Cursor.getDefaultCursor
    e.canvas.setCursor(cursor)

    Recipe.RequestFocusOnEnter.mouseEntered(e)
  }

  override def mouseWheelMoved(e: MouseWheelEvent): Unit = {
    Recipe.Scroll2D.mouseWheelMoved(e)
  }
}
