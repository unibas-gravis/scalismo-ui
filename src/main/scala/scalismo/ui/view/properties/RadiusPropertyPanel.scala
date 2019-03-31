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
import scalismo.ui.model.properties.{ HasRadius, NodeProperty }
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.util.FloatSlider

import scala.swing.BorderPanel
import scala.swing.event.ValueChanged

object RadiusPropertyPanel extends PropertyPanel.Factory {
  override def create(frame: ScalismoFrame): PropertyPanel = {
    new RadiusPropertyPanel(frame)
  }

  val MinValue: Float = 0.0f
  val MaxValue: Float = 25.0f
  val StepSize: Float = 0.1f
}

class RadiusPropertyPanel(override val frame: ScalismoFrame) extends BorderPanel with PropertyPanel {
  override def description: String = "Radius"

  private var targets: List[HasRadius] = Nil

  private val slider = new FloatSlider(RadiusPropertyPanel.MinValue, RadiusPropertyPanel.MaxValue, RadiusPropertyPanel.StepSize)

  layout(new BorderPanel {
    val sliderPanel = new BorderPanel {
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
    targets.headOption.foreach(t => slider.floatValue = t.radius.value.toFloat)
    listenToOwnEvents()
  }

  override def setNodes(nodes: List[SceneNode]): Boolean = {
    cleanup()
    val supported = allMatch[HasRadius](nodes)
    if (supported.nonEmpty) {
      targets = supported
      listenTo(targets.head.radius)
      updateUi()
      true
    } else false
  }

  def cleanup(): Unit = {
    targets.headOption.foreach(t => deafTo(t.radius))
    targets = Nil
  }

  reactions += {
    case NodeProperty.event.PropertyChanged(_) => updateUi()
    case ValueChanged(_) => targets.foreach(_.radius.value = slider.floatValue)
  }

}
