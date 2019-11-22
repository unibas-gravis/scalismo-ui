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

import de.sciss.swingplus.ComboBox
import javax.swing.border.TitledBorder
import scalismo.ui.model.SceneNode
import scalismo.ui.model.properties._
import scalismo.ui.view.ScalismoFrame

import scala.swing.BorderPanel
import scala.swing.event.SelectionChanged

object ColorMappingPropertyPanel extends PropertyPanel.Factory {
  override def create(frame: ScalismoFrame): PropertyPanel = {
    new ColorMappingPropertyPanel(frame)
  }

  // this is a var so users can override it, if needed.
  var StandardMappings: List[ColorMapping] = List(ColorMapping.BlueToRed, ColorMapping.BlackToWhite, ColorMapping.WhiteToBlack)
}

class ColorMappingPropertyPanel(override val frame: ScalismoFrame) extends BorderPanel with PropertyPanel {

  private var targets: List[HasScalarRange] = Nil

  override def description: String = "Color Mapping"

  val combo = new ComboBox[ColorMapping]()

  def listenToOwnEvents(): Unit = {
    listenTo(combo.selection)
  }

  def deafToOwnEvents(): Unit = {
    deafTo(combo.selection)
  }

  def updateUi(): Unit = {
    deafToOwnEvents()
    // rebuild list of mappings to display; start with built-ins
    var items = ColorMappingPropertyPanel.StandardMappings
    // add the ones used in selected nodes (might be custom ones)
    items ++= targets.map(_.scalarRange.value.colorMapping)
    combo.items = items.distinct
    // select the active mapping. If there are multiple in use, tough luck - show the one from the first target.
    targets.headOption.foreach { t => combo.selection.item = t.scalarRange.value.colorMapping }
    listenToOwnEvents()
  }

  override def setNodes(nodes: List[SceneNode]): Boolean = {
    cleanup()
    val supported = allMatch[HasScalarRange](nodes)
    if (supported.nonEmpty) {
      targets = supported
      targets.foreach(t => listenTo(t.scalarRange))
      updateUi()
      true
    } else false
  }

  def cleanup(): Unit = {
    targets.foreach(t => deafTo(t.scalarRange))
    targets = Nil
  }

  layout(new BorderPanel {
    val comboPanel: BorderPanel = new BorderPanel {
      border = new TitledBorder(null, description, TitledBorder.LEADING, 0, null, null)
      layout(combo) = BorderPanel.Position.Center
    }
    layout(comboPanel) = BorderPanel.Position.Center
  }) = BorderPanel.Position.North

  listenToOwnEvents()

  reactions += {
    case SelectionChanged(_) => targets.foreach { t =>
      t.scalarRange.value = t.scalarRange.value.copy(colorMapping = combo.selection.item)
    }
    case NodeProperty.event.PropertyChanged(_) => updateUi()
  }
}