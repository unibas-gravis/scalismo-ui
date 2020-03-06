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

package scalismo.ui.view

import scala.swing.{BorderPanel, Orientation, SplitPane}

class ModelPanel(val frame: ScalismoFrame) extends BorderPanel {

  def setupPanels(): Unit = {
    val verticalSplit = new SplitPane(Orientation.Horizontal, nodesPanel, propertiesPanel) {
      resizeWeight = 0.5
    }
    // for safety, keep this out of the constructor, to make sure
    // that preferredSize returns something sensible
    verticalSplit.dividerLocation = (verticalSplit.preferredSize.height * 0.5).toInt

    // disabled for now, as it interferes with automatic size adjustion
    //val horizontalSplit = Component.wrap(new ExpandablePane(JSplitPane.HORIZONTAL_SPLIT, verticalSplit.peer))
    layout(verticalSplit) = BorderPanel.Position.Center
  }

  val nodesPanel = new NodesPanel(frame)
  val propertiesPanel = new NodePropertiesPanel(frame)

  setupPanels()
}
