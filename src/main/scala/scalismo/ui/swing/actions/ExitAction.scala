package scalismo.ui.swing.actions

import scalismo.ui.swing.ScalismoFrame

import scala.swing.Action

class ExitAction(name: String, app: ScalismoFrame) extends Action(name) {
  def apply() = {
    app.closeOperation()
  }
}
