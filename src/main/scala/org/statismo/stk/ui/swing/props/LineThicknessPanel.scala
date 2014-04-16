package org.statismo.stk.ui.swing.props

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics

import scala.swing.BorderPanel
import scala.swing.Component
import scala.swing.Label
import scala.swing.Slider
import scala.swing.event.Event
import scala.swing.event.ValueChanged

import org.statismo.stk.ui.swing.util.{EdtSlider, ColorPickerPanel}

import javax.swing.JPanel
import javax.swing.border.TitledBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

import scala.language.reflectiveCalls
import org.statismo.stk.ui.visualization.{Visualization, VisualizationProperty}
import org.statismo.stk.ui.visualization.props.{HasLineThickness, HasColorAndOpacity}
import scala.collection.immutable

class LineThicknessPanel extends BorderPanel with VisualizationsPropertyPanel {
  type Target = Visualization[_] with HasLineThickness
  type TargetSeq = immutable.Seq[Target]
  val description = "Line width"
  private var target: Option[TargetSeq] = None

  private val thicknessSlider = new EdtSlider {
    min = 0
    max = 10
    value = 1
  }

  {
    val opacityPanel = new BorderPanel {
      layout(thicknessSlider) = BorderPanel.Position.Center
      layout(new Label("0")) = BorderPanel.Position.West
      layout(new Label("10.0")) = BorderPanel.Position.East
      border = new TitledBorder(null, description, TitledBorder.LEADING, 0, null, null)
    }
    layout(opacityPanel) = BorderPanel.Position.North
  }
  listenToOwnEvents()

  reactions += {
    case VisualizationProperty.ValueChanged(_) => updateUi()
    case ValueChanged(s) =>
      if (target.isDefined) {
        target.get.foreach(t => t.lineThickness.value = s.asInstanceOf[Slider].value)
      }
  }

  def listenToOwnEvents() = {
    listenTo(thicknessSlider)
  }

  def deafToOwnEvents() = {
    deafTo(thicknessSlider)
  }

  def cleanup() = {
    if (target.isDefined) {
      target.get.foreach{t => deafTo(t.lineThickness)}
      target = None
    }
  }

  override def setVisualizations(visualizations: immutable.Seq[Visualization[_]]): Boolean = {
    cleanup()
    val usable = visualizations.filter(v => v.isInstanceOf[Target]).asInstanceOf[TargetSeq]
    if (!usable.isEmpty) {
      target = Some(usable)
      updateUi()
      target.get.foreach{t => listenTo(t.lineThickness)}
      true
    } else {
      false
    }
  }

  def updateUi() = {
    if (target.isDefined) {
      deafToOwnEvents()
      thicknessSlider.value = target.get.head.lineThickness.value
      listenToOwnEvents()
    }
  }
}