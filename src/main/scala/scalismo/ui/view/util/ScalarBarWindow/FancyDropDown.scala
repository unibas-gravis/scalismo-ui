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

package scalismo.ui.view.ScalarBarWindow

import java.awt.event.ActionEvent
import javax.swing.JComboBox

class FancyDropDown(items: Array[String])
    extends scala.swing.Component with java.awt.event.ActionListener {

  def formattedValue(dropDownValue: Int): String = dropDownValue.toString

  override lazy val peer = new JComboBox(items)

  def +=(item: String): Unit = peer.addItem(item)
  def -=(item: String): Unit = peer.removeItem(item)
  def item: String = peer.getSelectedItem.asInstanceOf[String]
  // we start at the beginning position then
  def reset() {
    peer.setSelectedIndex(0)
  }

  peer.addActionListener(this)
  // this is to do a specific action that we override in order to get the result we want
  def actionPerformed(e: ActionEvent) {
    //TODO now we have to change the color scheme when the toggle changes
  }

}