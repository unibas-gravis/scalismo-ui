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

import scalismo.ui.model.SceneNode
import scalismo.ui.view.ScalismoFrame

import scala.swing.BorderPanel

/**
 * This class combines multiple property panels into a single one.
 * The resulting layout is similar to a (vertical) BoxLayout,
 * except that each component only takes as much space as it
 * actually needs. This is achieved by nesting BorderPanels.
 *
 * @param frame       top-level frame
 * @param description human-readable name
 * @param delegates   property panels to be combined.
 */
class CombinedPropertiesPanel(override val frame: ScalismoFrame, override val description: String, delegates: PropertyPanel*) extends BorderPanel with PropertyPanel {

  /*
  * This will return true if *any* of the delegates returns true,
  * and false only if *no* delegate returns true.
  * In other words: The panel is functional if a non-empty subset
  * of its delegates is functional. Non-functional delegates are hidden.
  */
  override def setNodes(nodes: List[SceneNode]): Boolean = {
    val ok = delegates.map(d => delegatedSetNodes(d, nodes)).foldLeft(false)({
      (x, y) => x || y
    })
    revalidate()
    ok
  }

  private def delegatedSetNodes(del: PropertyPanel, nodes: List[SceneNode]): Boolean = {
    val ok = del.setNodes(nodes)
    del.visible = ok
    ok
  }

  // constructor

  delegates.reverse.foldLeft(this: BorderPanel)({
    (panel, comp) =>
      val child = new BorderPanel
      child layout comp = BorderPanel.Position.Center
      panel layout child = BorderPanel.Position.North
      child
  })
}
