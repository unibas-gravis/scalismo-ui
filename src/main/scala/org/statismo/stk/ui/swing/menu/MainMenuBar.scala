package org.statismo.stk.ui.swing.menu

import scala.swing.MenuBar

import org.statismo.stk.ui.StatismoFrame

class MainMenuBar(implicit app: StatismoFrame) extends MenuBar {
  val fileMenu = new FileMenu
  val optionsMenu = new OptionsMenu
  val toolsMenu = new ToolsMenu
  val helpMenu = new HelpMenu
  contents ++= Seq(fileMenu, optionsMenu, toolsMenu, helpMenu)
}