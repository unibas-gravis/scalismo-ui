package org.statismo.stk.ui.swing.menu

import org.statismo.stk.ui.StatismoFrame
import scala.swing.{CheckMenuItem, MenuItem, Menu}

object ToolsMenu {
  val Name = "Tools"
}

class ToolsMenu(implicit app: StatismoFrame) extends Menu(ToolsMenu.Name) {
  contents += new ConsoleMenuItem
}

object ConsoleMenuItem {
  val Name = "Scala Console"
}

class ConsoleMenuItem extends CheckMenuItem(ConsoleMenuItem.Name) {
  //TODO: implement actual logic
}