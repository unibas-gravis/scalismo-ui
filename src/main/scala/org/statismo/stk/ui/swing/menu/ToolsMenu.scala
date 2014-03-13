package org.statismo.stk.ui.swing.menu

import org.statismo.stk.ui.StatismoFrame
import scala.swing.{CheckMenuItem, MenuItem, Menu}
import scala.swing.event.ButtonClicked
import org.statismo.stk.ui.swing.Console

object ToolsMenu {
  val Name = "Tools"
}

class ToolsMenu(implicit app: StatismoFrame) extends Menu(ToolsMenu.Name) {
  contents += new ConsoleMenuItem
}

object ConsoleMenuItem {
  val Name = "Scala Console"
}

class ConsoleMenuItem(implicit app: StatismoFrame) extends CheckMenuItem(ConsoleMenuItem.Name) {
  selected = app.console.visible
  listenTo(app.console)
  reactions += {
    case ButtonClicked(b) => app.console.visible = selected
    case Console.VisibilityChanged(c) => selected = c.visible
  }
}