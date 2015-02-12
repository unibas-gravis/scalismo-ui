package scalismo.ui.swing.menu

import scalismo.ui.swing.ScalismoFrame

import scala.swing.MenuBar

class MainMenuBar(implicit app: ScalismoFrame) extends MenuBar {
  val fileMenu = new FileMenu
  val optionsMenu = new OptionsMenu
  val toolsMenu = new ToolsMenu
  val helpMenu = new HelpMenu
  contents ++= Seq(fileMenu, optionsMenu, toolsMenu, helpMenu)
}