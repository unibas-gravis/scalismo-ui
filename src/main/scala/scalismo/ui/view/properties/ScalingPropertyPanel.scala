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
import scalismo.ui.model.properties.{HasRadius, HasScaling, NodeProperty}
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.util.FloatSlider

import scala.swing.BorderPanel
import scala.swing.event.ValueChanged

object ScalingPropertyPanel extends PropertyPanel.Factory {
  override def create(frame: ScalismoFrame): PropertyPanel = {
    new ScalingPropertyPanel(frame)
  }

  val MinValue: Float = 0.0f
  val MaxValue: Float = 5.0f
  val StepSize: Float = 0.05f
}

class ScalingPropertyPanel(override val frame: ScalismoFrame) extends BorderPanel with PropertyPanel {
  override def description: String = "Scaling factor"

  private var targets: List[HasScaling] = Nil

  private val slider =
    new FloatSlider(ScalingPropertyPanel.MinValue, ScalingPropertyPanel.MaxValue, ScalingPropertyPanel.StepSize)

  layout(new BorderPanel {
    private val sliderPanel = new BorderPanel {
      border = new TitledBorder(null, description, TitledBorder.LEADING, 0, null, null)
      layout(slider) = BorderPanel.Position.Center
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
    targets.headOption.foreach(t => slider.floatValue = t.scaling.value.toFloat)
    listenToOwnEvents()
  }

  override def setNodes(nodes: List[SceneNode]): Boolean = {
    cleanup()
    val supported = allMatch[HasScaling](nodes)
    if (supported.nonEmpty) {
      targets = supported
      listenTo(targets.head.scaling)
      updateUi()
      true
    } else false
  }

  def cleanup(): Unit = {
    targets.headOption.foreach(t => deafTo(t.scaling))
    targets = Nil
  }

  reactions += {
    case NodeProperty.event.PropertyChanged(_) => updateUi()
    case ValueChanged(_)                       => targets.foreach(_.scaling.value = slider.floatValue)
  }

}
