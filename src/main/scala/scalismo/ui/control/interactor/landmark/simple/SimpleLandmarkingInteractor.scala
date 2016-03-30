package scalismo.ui.control.interactor.landmark.simple

import java.awt.event.MouseEvent
import java.awt.{ Color, Cursor }

import scalismo.ui.control.interactor.Interactor.Verdict
import scalismo.ui.control.interactor.Interactor.Verdict.Pass
import scalismo.ui.control.interactor.{ Interactor, Recipe }
import scalismo.ui.model.{ LandmarkNode, SceneNode }
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.view.ScalismoFrame

import scala.swing.ToggleButton
import scala.swing.event.ButtonClicked

trait SimpleLandmarkingInteractor extends Interactor {
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

  override def mouseClicked(e: MouseEvent): Verdict = {

    if (landmarkingButton.selected) {
      Recipe.AddLandmarkOnClick.mouseClicked(e)
    } else {
      Pass
    }
  }

  // set the cursor to a crosshair if we're in landmarking mode
  override def mouseEntered(e: MouseEvent): Verdict = {
    val cursor = if (landmarkingButton.selected) Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) else Cursor.getDefaultCursor
    e.canvas.setCursor(cursor)
    super.mouseEntered(e)
  }

  override def mouseMoved(e: MouseEvent): Verdict = {
    if (landmarkingButton.selected) {
      def exceptLandmarks(node: SceneNode) = node match {
        case nope: LandmarkNode => false
        case _ => true
      }
      Recipe.HighlightOutlineOfPickableObject.mouseMoved(e, exceptLandmarks)
    }
    super.mouseMoved(e)
  }

}
