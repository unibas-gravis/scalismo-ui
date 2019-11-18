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

import java.awt.event.ActionEvent
import javax.swing.border.TitledBorder

import scalismo.ui.model.SceneNode
import scalismo.ui.model.properties._
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.util.FancyDropDown

import scala.swing.BorderPanel

object ColorSchemePropertyPanel extends PropertyPanel.Factory {
  override def create(frame: ScalismoFrame): PropertyPanel = {
    new ColorSchemePropertyPanel(frame)
  }
}

class ColorSchemePropertyPanel(override val frame: ScalismoFrame) extends BorderPanel with PropertyPanel {

  private var targets: List[HasScalarRange] = Nil

  override def description: String = "ColorScheme"

  val items = Array("Blue-Red", "Black-White", "White-Black")

  val dropDown: FancyDropDown = new FancyDropDown(items) {
    override def actionPerformed(e: ActionEvent): Unit = {
      updateColorMapping()
    }
  }

  listenToOwnEvents()

  def listenToOwnEvents(): Unit = {
    listenTo(dropDown)
  }

  def deafToOwnEvents(): Unit = {
    deafTo(dropDown)
  }

  def updateColorMapping() {
    if (dropDown.item == items(0)) {
      targets.foreach(t => {
        t.scalarRange.colorMapping = BlueToRedColorMapping
      })
    } else if (dropDown.item == items(1)) {
      targets.foreach(t => {
        t.scalarRange.colorMapping = BlackToWhiteMapping
      })
    } else if (dropDown.item == items(2)) {
      targets.foreach(t => {
        t.scalarRange.colorMapping = WhiteToBlackMapping
      })
    }

    targets.foreach(t => t.scalarRange.value = t.scalarRange.value.copy())
  }

  override def setNodes(nodes: List[SceneNode]): Boolean = {
    cleanup()
    val supported = allMatch[HasScalarRange](nodes)
    if (supported.nonEmpty) {
      targets = supported
      listenTo(targets.head.scalarRange)
      true
    } else false
  }

  def cleanup(): Unit = {
    targets.headOption.foreach(t => deafTo(t.scalarRange))
    targets = Nil
  }

  layout(new BorderPanel {
    val dropDownPanel: BorderPanel = new BorderPanel {
      border = new TitledBorder(null, description, TitledBorder.LEADING, 0, null, null)
      layout(dropDown) = BorderPanel.Position.Center
    }
    layout(dropDownPanel) = BorderPanel.Position.Center
  }) = BorderPanel.Position.North
}