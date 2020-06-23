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

import javax.swing.border.TitledBorder
import scalismo.ui.model.SceneNode
import scalismo.ui.model.properties.{HasLineWidth, LineWidthProperty, NodeProperty}
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.util.{TypedSlider, TypedSliderValueChanged}

import scala.swing.BorderPanel

object LineWidthPropertyPanel extends PropertyPanel.Factory {
  override def create(frame: ScalismoFrame): PropertyPanel = {
    new LineWidthPropertyPanel(frame)
  }
}

class LineWidthPropertyPanel(override val frame: ScalismoFrame) extends BorderPanel with PropertyPanel {
  override def description: String = "2D Outline Width"

  private var targets: List[HasLineWidth] = Nil

  private val slider = new TypedSlider[Int](showLabels = true) {
    min = 1
    max = LineWidthProperty.MaxValue
    value = 1
  }

  layout(new BorderPanel {
    val sliderPanel: BorderPanel = new BorderPanel {
      border = new TitledBorder(null, description, TitledBorder.LEADING, 0, null, null)
      layout(slider.slider) = BorderPanel.Position.Center
    }
    layout(sliderPanel) = BorderPanel.Position.Center
  }) = BorderPanel.Position.North

  listenToOwnEvents()

  def listenToOwnEvents(): Unit = {
    listenTo(slider)
  }

  def deafToOwnEvents(): Unit = {
    deafTo(slider)
  }

  def updateUi(): Unit = {
    deafToOwnEvents()
    targets.headOption.foreach(t => slider.value = t.lineWidth.value)
    listenToOwnEvents()
  }

  override def setNodes(nodes: List[SceneNode]): Boolean = {
    cleanup()
    val supported = allMatch[HasLineWidth](nodes)
    if (supported.nonEmpty) {
      targets = supported
      listenTo(targets.head.lineWidth)
      updateUi()
      true
    } else false
  }

  def cleanup(): Unit = {
    targets.headOption.foreach(t => deafTo(t.lineWidth))
    targets = Nil
  }

  reactions += {
    case NodeProperty.event.PropertyChanged(_) => updateUi()
    case TypedSliderValueChanged(_)            => targets.foreach(_.lineWidth.value = slider.value)
  }

}
