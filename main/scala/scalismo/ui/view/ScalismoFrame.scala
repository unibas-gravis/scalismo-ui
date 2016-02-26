package scalismo.ui.view

import javax.swing.WindowConstants

import scalismo.ui.model.Scene
import scalismo.ui.util.EdtUtil
import scalismo.ui.view.menu.FileMenu
import scalismo.ui.view.menu.FileMenu.ExitItem

import scala.swing.{MenuBar, BorderPanel, MainFrame}

object ScalismoFrame {
  type Constructor = (Scene => ScalismoFrame)

  def defaultConstructor: Constructor = {
    s: Scene => new ScalismoFrame(s)
  }

  def apply(scene: Scene = new Scene, constructor: Constructor = defaultConstructor): ScalismoFrame = {
    EdtUtil.onEdtWithResult(constructor(scene))
  }
}

class ScalismoFrame(val scene: Scene) extends MainFrame {

  implicit val frame = this

  def setup(args: Array[String]): Unit = {
    setupMenus()
    setupToolbar()
    setupPanels()
    setupBehavior()
  }

  def setupMenus(): Unit = {
    val fileMenu = new FileMenu
    fileMenu.contents += new ExitItem
    menuBar.contents += fileMenu
  }

  def setupToolbar(): Unit = {
  }

  def setupPanels(): Unit = {
    val root = new BorderPanel
    root.layout(toolBar) = BorderPanel.Position.North
    root.layout(modelPanel) = BorderPanel.Position.West
    root.layout(statusBar) = BorderPanel.Position.South
    root.layout(perspectivePanel) = BorderPanel.Position.Center

    this.contents = root
  }

  def setupBehavior(): Unit = {
    peer.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
    centerOnScreen()
  }

  override def closeOperation(): Unit = {
    dispose()
  }

  val toolBar = new ToolBar
  val modelPanel = new ModelPanel
  val perspectivePanel = new PerspectivePanel
  val statusBar = new StatusBar

  menuBar = new MenuBar

}
