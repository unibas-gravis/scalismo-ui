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

import java.awt.{Color, Dimension, Graphics}

import javax.swing.JPanel
import javax.swing.border.{LineBorder, TitledBorder}
import scalismo.ui.model.SceneNode
import scalismo.ui.model.properties.{HasColor, HasOpacity, NodeProperty, OpacityProperty}
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.util.ColorChooserPanel.event.ColorChanged
import scalismo.ui.view.util.ScalableUI.implicits._
import scalismo.ui.view.util.{ColorChooserPanel, Constants, ScalableUI}

import scala.swing.{BorderPanel, Component}

object ColorPropertyPanel extends PropertyPanel.Factory {
  override def create(frame: ScalismoFrame): PropertyPanel = {
    new ColorPropertyPanel(frame)
  }
}

class ColorPropertyPanel(override val frame: ScalismoFrame) extends BorderPanel with PropertyPanel {
  override def description: String = "Color"

  private var targets: List[HasColor] = Nil

  class ColorDisplayer extends Component {
    private val BorderWidth = 1.scaled

    override lazy val peer: JPanel = new JPanel {
      override def paintComponent(g: Graphics): Unit = {
        val dim: Dimension = getSize
        val s = BorderWidth
        g.setColor(Constants.PerceivedBackgroundColor)
        g.fillRect(s, s, dim.width - s, dim.height - s)
        // now paint the selected color on the gray background
        g.setColor(getBackground)
        g.fillRect(s, s, dim.width - s, dim.height - s)
      }
    }

    def setColor(color: Color, opacity: Double): Unit = {
      val comp = color.getColorComponents(null)
      val c = new Color(comp(0), comp(1), comp(2), opacity.toFloat)
      peer.setBackground(c)
      peer.setForeground(c)
      revalidate()
      repaint()
    }

    peer.setOpaque(false)
    peer.setPreferredSize(ScalableUI.scaleDimension(new Dimension(20, 20)))
    peer.setBorder(new LineBorder(Color.BLACK, BorderWidth, false))
  }

  val colorDisplayer = new ColorDisplayer

  private val colorChooser = new ColorChooserPanel {
    border = new javax.swing.border.EmptyBorder(0, 0, 10.scaled, 0)
  }

  {
    val northedPanel: BorderPanel = new BorderPanel {
      val colorPanel: BorderPanel = new BorderPanel {
        border = new TitledBorder(null, description, TitledBorder.LEADING, 0, null, null)
        layout(colorChooser) = BorderPanel.Position.Center
        layout(colorDisplayer) = BorderPanel.Position.South
      }
      layout(colorPanel) = BorderPanel.Position.Center
    }
    layout(northedPanel) = BorderPanel.Position.North
  }

  listenToOwnEvents()

  private def listenToOwnEvents(): Unit = {
    listenTo(colorChooser)
  }

  private def deafToOwnEvents(): Unit = {
    deafTo(colorChooser)
  }

  private def updateUi(): Unit = {
    if (targets.nonEmpty) {
      deafToOwnEvents()
      updateColorDisplayer()
      listenToOwnEvents()
    }
  }

  def updateColorDisplayer(): Unit = {
    targets.headOption.foreach { t =>
      val c = t.color.value
      colorChooser.color = c
      colorDisplayer.setColor(c, targetOpacityOption().map(_.value).getOrElse(1.0))
    }
  }

  // returns the target's opacity property if the (first) target also happens to be a HasOpacity, else None
  def targetOpacityOption(): Option[OpacityProperty] = {
    targets.headOption.collect { case ok: HasOpacity => ok.opacity }
  }

  override def setNodes(nodes: List[SceneNode]): Boolean = {
    cleanup()
    val supported = allMatch[HasColor](nodes)
    if (supported.nonEmpty) {
      targets = supported
      listenTo(targets.head.color)
      targetOpacityOption().foreach(o => listenTo(o))
      updateUi()
      true
    } else false
  }

  def cleanup(): Unit = {
    targets.headOption.foreach(t => deafTo(t.color))
    targetOpacityOption().foreach(o => deafTo(o))
    targets = Nil
  }

  reactions += {
    case NodeProperty.event.PropertyChanged(_) => updateUi()
    case ColorChanged(c) =>
      targets.foreach { t =>
        t.color.value = c.color
        updateColorDisplayer()
      }
  }

}
