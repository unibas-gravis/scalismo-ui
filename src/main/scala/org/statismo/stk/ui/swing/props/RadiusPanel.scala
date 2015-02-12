package org.statismo.stk.ui.swing.props

import scala.swing.BorderPanel
import scala.swing.Label
import scala.swing.Slider
import scala.swing.event.ValueChanged


import javax.swing.border.TitledBorder
import org.statismo.stk.ui.visualization.props.HasRadiuses
import org.statismo.stk.ui.visualization.{Visualization, VisualizationProperty}
import scala.collection.immutable
import org.statismo.stk.ui.swing.util.EdtSlider

class RadiusPanel extends BorderPanel with VisualizationsPropertyPanel {
  type Target = Visualization[_] with HasRadiuses[_]
  type TargetSeq = immutable.Seq[Target]

  val description = "Radius"
  private var target: Option[TargetSeq] = None

  private val slider = new EdtSlider {
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
        val f = s.asInstanceOf[Slider].value.toFloat
        target.get.foreach(_.radiuses.setAll(f))
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
      target.get.foreach(t => deafTo(t.radiuses))
      target = None
    }
  }

  override def setVisualizations(visualizations: immutable.Seq[Visualization[_]]): Boolean = {
    cleanup()
    val usable = visualizations.filter(v => v.isInstanceOf[Target]).asInstanceOf[TargetSeq]

    if (usable.nonEmpty) {
      target = Some(usable)
      updateUi()
      target.get.foreach(t => listenTo(t.radiuses))
      true
    } else {
      false
    }
  }

  def updateUi() = {
    if (target.isDefined) {
      deafToOwnEvents()
      slider.value = target.get.head.radiuses.value(0).toInt
      listenToOwnEvents()
    }
  }
}