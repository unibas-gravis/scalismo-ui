package scalismo.ui.swing.props

import javax.swing.border.TitledBorder

import scalismo.ui.swing.util.EdtSlider

import scala.swing.{ BorderPanel, Label }

class ThreeDImagePlanePanel extends BorderPanel with PropertyPanel {
  val description = "Slice position"
  private var target: Option[Boolean] = None

  val minLabel = new Label("0")
  val maxLabel = new Label("1000")

  val title = new TitledBorder(null, "Slice position", TitledBorder.LEADING, 0, null, null)
  private val slider = new EdtSlider {
    min = 0
    max = 1
    value = 0
  }

  {
    val northedPanel = new BorderPanel {
      layout(new BorderPanel {
        layout(slider) = BorderPanel.Position.Center
        layout(minLabel) = BorderPanel.Position.West
        layout(maxLabel) = BorderPanel.Position.East
        border = title
      }) = BorderPanel.Position.Center
    }
    layout(northedPanel) = BorderPanel.Position.North
  }
  listenToOwnEvents()

  //  reactions += {
  //    case ValueChanged(s) =>
  //      if (target.isDefined) {
  //        target.get.position = s.asInstanceOf[Slider].value
  //      }
  //    case ThreeDImagePlane.PositionChanged(s) =>
  //      updateUi()
  //  }

  def listenToOwnEvents() = {
    listenTo(slider)
  }

  def deafToOwnEvents() = {
    deafTo(slider)
  }

  def cleanup() = {
    if (target.isDefined) {
      //      deafTo(target.get)
      target = None
    }
  }

  def setObject(obj: Option[AnyRef]): Boolean = {
    //    cleanup()
    //    if (obj.isDefined && obj.get.isInstanceOf[ThreeDImagePlane]) {
    //      target = Some(obj.get.asInstanceOf[ThreeDImagePlane])
    //      updateUi()
    //      listenTo(target.get)
    //      true
    //    } else {
    //      false
    //    }
    false
  }

  def updateUi() = {
    if (target.isDefined) {
      //      deafToOwnEvents()
      //      slider.min = target.get.minPosition
      //      slider.max = target.get.maxPosition
      //      minLabel.text = target.get.minPosition.toString
      //      maxLabel.text = target.get.maxPosition.toString
      //      slider.value = target.get.position
      //      title.setTitle(description + " (" + target.get.name + ")")
      //      listenToOwnEvents()
    }
  }
}