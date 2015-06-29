package scalismo.ui.swing.actions

import scalismo.ui.swing.ScalismoFrame

import scala.swing.Action

class QuitAction(name: String, app: ScalismoFrame) extends Action(name) {
  def apply() = {
    app.closeOperation()
  }
}