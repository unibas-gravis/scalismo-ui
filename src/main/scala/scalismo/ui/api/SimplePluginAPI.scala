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

package scalismo.ui.api

import scalismo.ui.model.StatusMessage
import scalismo.ui.util.EdtUtil

import scala.swing.Component

trait SimplePluginAPI {

  def ui: ScalismoUI

  def activate(): Unit = {

    onActivated()
  }

  def deactivate(): Unit = {
    onDeactivated()
  }

  def message(message: String): Unit = {
    ui.frame.status.set(StatusMessage(message))
  }

  def message(message: StatusMessage): Unit = {
    ui.frame.status.set(StatusMessage(message.text, message.kind))
  }

  def addToToolbar(panel: Component): Unit = {
    EdtUtil.onEdt {
      ui.frame.toolbar.add(panel)
    }
  }

  def removeFromToolbar(panel: Component): Unit = {
    EdtUtil.onEdt {
      ui.frame.toolbar.remove(panel)
    }
  }

  def onActivated(): Unit

  def onDeactivated(): Unit

}
