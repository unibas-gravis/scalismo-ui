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

package scalismo.ui.view.action

import javax.swing.{ Icon, UIManager }
import scalismo.ui.view.ScalismoFrame

import scala.swing.Dialog.Message
import scala.swing.Swing.EmptyIcon
import scala.swing.{ Action, Dialog }

class AskForInputAction[A](message: String, initial: A, callback: Option[A] => Unit, title: String = UIManager.getString("OptionPane.inputDialogTitle"), entries: List[A] = Nil, messageType: Message.Value = Message.Question, icon: Icon = EmptyIcon)(implicit frame: ScalismoFrame) extends Action(title) {
  override def apply(): Unit = {
    val result = Dialog.showInput[A](frame.componentForDialogs, message, title, messageType, icon, entries, initial)
    callback(result)
  }
}
