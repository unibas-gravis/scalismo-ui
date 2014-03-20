package org.statismo.stk.ui.swing.props

import scala.swing.BorderPanel
import scala.swing.Label
import scala.swing.Slider
import scala.swing.event.ValueChanged

import org.statismo.stk.ui.SceneTreeObject
import org.statismo.stk.ui.ThreeDImagePlane

import javax.swing.border.TitledBorder

class ThreeDImagePlanePanel extends BorderPanel with PropertyPanel {
  val description = "Slice position"
  private var target: Option[ThreeDImagePlane[_]] = None

  val minLabel = new Label("0")
  val maxLabel = new Label("1000")

  val title = new TitledBorder(null, "Slice position", TitledBorder.LEADING, 0, null, null)
  private val slider = new Slider() {
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

  reactions += {
    case ValueChanged(s) =>
      if (target.isDefined) {
        target.get.position = s.asInstanceOf[Slider].value
      }
    case ThreeDImagePlane.PositionChanged(s) =>
      updateUi()
  }

  def listenToOwnEvents() = {
    listenTo(slider)
  }

  def deafToOwnEvents() = {
    deafTo(slider)
  }

  def cleanup() = {
    if (target.isDefined) {
      deafTo(target.get)
      target = None
    }
  }

  def setObject(obj: Option[AnyRef]): Boolean = {
    cleanup()
    if (obj.isDefined && obj.get.isInstanceOf[ThreeDImagePlane[_]]) {
      target = Some(obj.get.asInstanceOf[ThreeDImagePlane[_]])
      updateUi()
      listenTo(target.get)
      true
    } else {
      false
    }
  }

  def updateUi() = {
    if (target.isDefined) {
      deafToOwnEvents()
      slider.min = target.get.minPosition
      slider.max = target.get.maxPosition
      minLabel.text = target.get.minPosition.toString
      maxLabel.text = target.get.maxPosition.toString
      slider.value = target.get.position
      title.setTitle(description + " (" + target.get.name + ")")
      listenToOwnEvents()
    }
  }
}