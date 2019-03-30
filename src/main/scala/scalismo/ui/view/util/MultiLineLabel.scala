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

package scalismo.ui.view.util

import java.awt.Color

import javax.swing.{ BorderFactory, UIManager }

import scala.swing.TextArea

class MultiLineLabel(text: String) extends TextArea(text) {
  peer.setLineWrap(true)
  peer.setWrapStyleWord(true)
  peer.setEditable(false)
  peer.setCursor(null)
  peer.setOpaque(false)
  peer.setFocusable(false)
  peer.setBackground(new Color(UIManager.getColor("control").getRGB))
  val hv = ScalableUI.scale(10)
  peer.setBorder(BorderFactory.createEmptyBorder(hv, hv, hv, hv))

}
