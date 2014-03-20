package org.statismo.stk.ui.swing.props

import scala.swing.BorderPanel
import scala.swing.Label
import scala.swing.Slider
import scala.swing.event.ValueChanged

import org.statismo.stk.ui.SceneTreeObject

import javax.swing.border.TitledBorder
import org.statismo.stk.ui.visualization.props.{HasColorAndOpacity, HasRadius}
import org.statismo.stk.ui.visualization.{Visualization, VisualizationProperty}
import scala.collection.immutable

class RadiusPanel extends BorderPanel with VisualizationsPropertyPanel {
  type Target = Visualization[_] with HasRadius
  type TargetSeq = immutable.Seq[Target]

  val description = "Radius"
  private var target: Option[TargetSeq] = None

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
        target.get.foreach(_.radius.value = s.asInstanceOf[Slider].value.toFloat)
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
      target.get.foreach(t => deafTo(t.radius))
      target = None
    }
  }

  override def setVisualizations(visualizations: immutable.Seq[Visualization[_]]): Boolean = {
    cleanup()
    val usable = visualizations.filter(v => v.isInstanceOf[Target]).asInstanceOf[TargetSeq]

    if (!usable.isEmpty) {
      target = Some(usable)
      updateUi()
      target.get.foreach(t => listenTo(t.radius))
      true
    } else {
      false
    }
  }

  def updateUi() = {
    if (target.isDefined) {
      deafToOwnEvents()
      slider.value = target.get.head.radius.value.toInt
      listenToOwnEvents()
    }
  }
}