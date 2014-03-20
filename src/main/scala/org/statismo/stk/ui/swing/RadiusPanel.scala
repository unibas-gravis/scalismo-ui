package org.statismo.stk.ui.swing

import scala.swing.BorderPanel
import scala.swing.Label
import scala.swing.Slider
import scala.swing.event.ValueChanged

import org.statismo.stk.ui.SceneTreeObject

import javax.swing.border.TitledBorder
import org.statismo.stk.ui.visualization.props.HasRadius
import org.statismo.stk.ui.visualization.VisualizationProperty

class RadiusPanel extends BorderPanel with SceneObjectPropertyPanel {
  val description = "Radius"
  private var target: Option[HasRadius] = None

  private val slider = new Slider() {
    min = 1
    max = 20
    value = 0
  }

  {
    val northedPanel = new BorderPanel {
      val opacityPanel = new BorderPanel {
        layout(slider) = BorderPanel.Position.Center
        layout(new Label(slider.min.toString)) = BorderPanel.Position.West
        layout(new Label(slider.max.toString)) = BorderPanel.Position.East
        border = new TitledBorder(null, "Radius", TitledBorder.LEADING, 0, null, null)
      }
      layout(opacityPanel) = BorderPanel.Position.Center
    }
    layout(northedPanel) = BorderPanel.Position.North
  }
  listenToOwnEvents()

  reactions += {
    case ValueChanged(s) =>
      if (target.isDefined) {
        target.get.radius.value = s.asInstanceOf[Slider].value.toFloat
      }
    case VisualizationProperty.ValueChanged(_) => updateUi()
  }

  def listenToOwnEvents() = {
    listenTo(slider)
  }

  def deafToOwnEvents() = {
    deafTo(slider)
  }

  def cleanup() = {
    if (target.isDefined) {
      deafTo(target.get.radius)
      target = None
    }
  }

  def setObject(obj: Option[SceneTreeObject]): Boolean = {
    cleanup()
    if (obj.isDefined && obj.get.isInstanceOf[HasRadius]) {
      target = Some(obj.get.asInstanceOf[HasRadius])
      updateUi()
      listenTo(target.get.radius)
      true
    } else {
      false
    }
  }

  def updateUi() = {
    if (target.isDefined) {
      deafToOwnEvents()
      slider.value = target.get.radius.value.toInt
      listenToOwnEvents()
    }
  }
}