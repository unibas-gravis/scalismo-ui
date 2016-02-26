package scalismo.ui.view.action

import scalismo.ui.view.ScalismoFrame

import scala.swing.Action

class ExitAction(title: String)(implicit frame: ScalismoFrame) extends Action(title) {
  override def apply(): Unit = {
    frame.closeOperation()
  }
}
