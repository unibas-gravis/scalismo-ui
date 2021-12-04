/*
 * Copyright (C) 2016  University of Basel, Graphics and Vision Research Group
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scalismo.ui.view.properties

import java.awt.Color

import javax.swing.BorderFactory
import javax.swing.border.TitledBorder
import scalismo.ui.model.SceneNode
import scalismo.ui.model.properties.{ColorMapping, HasScalarRange, NodeProperty}
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.util.ScalableUI.implicits.scalableInt
import scalismo.ui.view.util.{FancySlider, MultiLineLabel}

import scala.swing.BorderPanel
import scala.swing.event.ValueChanged

object ScalarRangePropertyPanel extends PropertyPanel.Factory {
  override def create(frame: ScalismoFrame): PropertyPanel = {
    new ScalarRangePropertyPanel(frame)
  }
}

class ScalarRangePropertyPanel(override val frame: ScalismoFrame) extends BorderPanel with PropertyPanel {
  override def description: String = "Scalar Range Mapping Thresholds"

  private var targets: List[HasScalarRange] = Nil
  private var min: Float = 0
  private var max: Float = 100
  private var step: Float = 1

  private def colorizeSliderValue(slider: FancySlider, color: Color): Unit = {
    // Setting the color of the displayed text may result in poor readability for certain colors, so we
    // create an "underline" showing the color instead.
    slider.valueLabel.border = BorderFactory.createMatteBorder(0, 0, 3.scaled, 0, color)
  }

  private val minimumSlider = new FancySlider {
    this.min = 0
    this.max = 100
    this.value = 0

    override def formattedValue(sliderValue: Int): String = formatSliderValue(sliderValue)

    colorizeSliderValue(this, ColorMapping.Default.lowerColor)
  }

  private val maximumSlider = new FancySlider {
    this.min = 0
    this.max = 100
    this.value = 100

    override def formattedValue(sliderValue: Int): String = formatSliderValue(sliderValue)

    colorizeSliderValue(this, ColorMapping.Default.upperColor)
  }

  private val mismatchMessage = new MultiLineLabel(
    "Scalar range domain mismatch. Please multi-select only nodes with matching domains."
  )

  {
    val northedPanel: BorderPanel = new BorderPanel {
      private val slidersPanel = new BorderPanel {
        border = new TitledBorder(null, description, TitledBorder.LEADING, 0, null, null)
        layout(minimumSlider) = BorderPanel.Position.North
        layout(maximumSlider) = BorderPanel.Position.South
        layout(mismatchMessage) = BorderPanel.Position.Center
      }
      layout(slidersPanel) = BorderPanel.Position.Center
    }
    layout(northedPanel) = BorderPanel.Position.North
  }

  listenToOwnEvents()

  def listenToOwnEvents(): Unit = {
    listenTo(minimumSlider, maximumSlider)
  }

  def deafToOwnEvents(): Unit = {
    deafTo(minimumSlider, maximumSlider)
  }

  def toSliderValue(v: Float): Int = {
    if (step == 0) 0 else Math.round((v - min) / step)
  }

  def formatSliderValue(i: Int): String = {
    if (step == 0) "0"
    else if (step >= 1) f"${fromSliderValue(i)}%2.0f"
    else if (step >= .1) f"${fromSliderValue(i)}%2.1f"
    else if (step >= .01) f"${fromSliderValue(i)}%2.2f"
    else f"${fromSliderValue(i)}%2.3f"
  }

  def fromSliderValue(v: Int): Float = {
    v * step + min
  }

  def updateUi(): Unit = {
    deafToOwnEvents()

    // either show sliders, or information
    mismatchMessage.visible = targets.isEmpty
    minimumSlider.visible = targets.nonEmpty
    maximumSlider.visible = targets.nonEmpty

    targets.headOption.foreach { t =>
      val range = t.scalarRange.value
      min = range.domainMinimum
      max = range.domainMaximum
      step = (max - min) / 100.0f

      // this is an ugly workaround to make sure (min, max) values are properly displayed
      def reinitSlider(s: FancySlider): Unit = {
        s.min = 1
        s.min = 0
        s.max = 99
        s.max = 100
      }

      reinitSlider(minimumSlider)
      reinitSlider(maximumSlider)

      minimumSlider.value = toSliderValue(range.mappedMinimum)
      maximumSlider.value = toSliderValue(range.mappedMaximum)

      colorizeSliderValue(minimumSlider, range.colorMapping.lowerColor)
      colorizeSliderValue(maximumSlider, range.colorMapping.upperColor)

    }
    listenToOwnEvents()
  }

  override def setNodes(nodes: List[SceneNode]): Boolean = {
    cleanup()
    val supported = allMatch[HasScalarRange](nodes)
    if (supported.nonEmpty) {

      // check if all nodes have the same scalar range domain
      val ranges = supported.map(_.scalarRange.value)
      val minima = ranges.map(_.domainMinimum).distinct.length
      val maxima = ranges.map(_.domainMaximum).distinct.length

      if (minima == 1 && maxima == 1) {
        targets = supported
      } else {
        // can't show a single set of sliders for multiple domains.
        // However, we'll still show the UI, with a message instead.
        targets = Nil
      }
      targets.foreach(t => listenTo(t.scalarRange))
      updateUi()
      true
    } else false
  }

  def cleanup(): Unit = {
    targets.foreach(t => deafTo(t.scalarRange))
    targets = Nil
  }

  reactions += {
    case NodeProperty.event.PropertyChanged(_) => updateUi()
    case ValueChanged(slider) =>
      deafToOwnEvents()
      if (maximumSlider.value < minimumSlider.value) {
        if (slider eq minimumSlider) maximumSlider.value = minimumSlider.value
        else if (slider eq maximumSlider) minimumSlider.value = maximumSlider.value
      }
      propagateSliderChanges()
      listenToOwnEvents()
    //target.foreach(_.opacity.value = minimumSlider.value.toFloat / 100.0f)
  }

  def propagateSliderChanges(): Unit = {
    val (fMin, fMax) = (fromSliderValue(minimumSlider.value), fromSliderValue(maximumSlider.value))
    targets.foreach(t => t.scalarRange.value = t.scalarRange.value.copy(mappedMinimum = fMin, mappedMaximum = fMax))
  }

}
