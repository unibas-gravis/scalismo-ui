package scalismo.ui.swing.props

import javax.swing.border.TitledBorder

import scalismo.ui.Scene
import scalismo.ui.swing.util.FancySlider

import scala.swing._
import scala.swing.event.{ ButtonClicked, ValueChanged }

class SceneOptionsPanel extends BorderPanel with PropertyPanel {
  override def description: String = "Options"

  private var target: Option[Scene] = None

  override def setObject(obj: Option[AnyRef]): Boolean = {
    cleanup()
    obj match {
      case Some(s: Scene) =>
        target = Some(s)
        updateUi()
        true
      case _ => false
    }
  }

  def cleanup(): Unit = {
    target = None
  }

  def listenToOwnEvents() = {
    listenTo(radiusSlider, highlightCheckbox)
  }

  def deafToOwnEvents() = {
    deafTo(radiusSlider, highlightCheckbox)
  }

  reactions += {
    case ValueChanged(s) if s eq radiusSlider => target.foreach(_.options.twoDLandmarking.snapRadius = radiusSlider.value)
    case ButtonClicked(s) if s eq highlightCheckbox => target.foreach(_.options.twoDLandmarking.highlightClosest = highlightCheckbox.selected)
  }

  def updateUi(): Unit = {
    deafToOwnEvents()
    target.foreach { s =>
      radiusSlider.value = Math.round(s.options.twoDLandmarking.snapRadius)
      highlightCheckbox.selected = s.options.twoDLandmarking.highlightClosest
    }
    listenToOwnEvents()
  }

  val radiusSlider = new FancySlider {
    min = 0
    max = 50
  }

  val highlightCheckbox = new CheckBox("Highlight candidate contour") {
    tooltip = "Real-time highlighting of closest contour within threshold. Try disabling this if you experience performance problems."
  }

  {
    val northedPanel = new BorderPanel {
      val twoDOptionsPanel = new GridPanel(3, 1) {
        border = new TitledBorder(null, "2D Landmark clicking", TitledBorder.LEADING, 0, null, null)
        contents += new Label("Snap to contours when distance (in mm) is at most:") {
          horizontalAlignment = Alignment.Left
        }
        contents += radiusSlider
        contents += highlightCheckbox
      }
      layout(twoDOptionsPanel) = BorderPanel.Position.Center
    }
    layout(northedPanel) = BorderPanel.Position.North
  }

}
