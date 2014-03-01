package org.statismo.stk.ui.swing.actions

import scala.swing.Action

import org.statismo.stk.ui.StatismoFrame

class QuitAction(name: String, app: StatismoFrame) extends Action(name) {
  def apply() = {
    app.closeOperation()
  }
}