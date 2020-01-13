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

import java.awt.event.{MouseAdapter, MouseEvent}
import java.awt.{Color, Cursor, Desktop}
import java.net.URI

import javax.swing.Icon

import scala.swing.Swing.EmptyIcon
import scala.swing.{Alignment, Label}

object LinkLabel {

  lazy val desktop: Option[Desktop] = {
    if (!Desktop.isDesktopSupported) None
    else {
      val desktop = Option(Desktop.getDesktop)
      if (desktop.nonEmpty && desktop.forall(_.isSupported(Desktop.Action.BROWSE))) desktop else None
    }
  }
}

class LinkLabel(text: String,
                uri: URI,
                icon: Icon = EmptyIcon,
                alignment: Alignment.Value = Alignment.Center,
                preventLinkStyle: Boolean = false,
                preventTooltip: Boolean = false)
    extends Label(text, icon, alignment) {
  if (!preventTooltip) {
    tooltip = uri.toString
  }

  // this will only kick in if the desktop can actually open links
  LinkLabel.desktop.foreach { d =>
    if (!preventLinkStyle) {
      foreground = Color.BLUE.darker()
      //      val attributes = font.getAttributes
      //      attributes.asInstanceOf[java.util.Map[Object, Object]].put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON)
      //      font = font.deriveFont(attributes)
    }

    cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    peer.addMouseListener(new MouseAdapter {
      override def mouseClicked(e: MouseEvent): Unit = {
        if (e.getClickCount == 1) {
          d.browse(uri)
        }
      }
    })
  }
}
