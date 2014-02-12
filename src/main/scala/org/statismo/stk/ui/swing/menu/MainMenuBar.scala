package org.statismo.stk.ui.swing.menu

import scala.swing.MenuBar
import org.statismo.stk.ui.StatismoFrame

class MainMenuBar(implicit app: StatismoFrame) extends MenuBar {
  val fileMenu = new FileMenu
  val helpMenu = new HelpMenu
  val optionsMenu = new OptionsMenu
  contents ++= Seq(fileMenu, optionsMenu, helpMenu)
}