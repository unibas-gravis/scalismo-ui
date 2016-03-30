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
