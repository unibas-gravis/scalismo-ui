package scalismo.ui.view

import javax.swing.{ SwingUtilities, WindowConstants }

import scalismo.ui.model.Scene
import scalismo.ui.view.menu.HelpMenu.AboutItem
import scalismo.ui.view.menu.{ HelpMenu, FileMenu }
import scalismo.ui.view.menu.FileMenu.ExitItem

import scala.swing.{ BorderPanel, MainFrame, MenuBar }

/**
 * A ScalismoFrame is the top-level view object of every application using the Scalismo UI.
 *
 * Note that because we use VTK, and that is rather shaky with multithreading,
 * a ScalismoFrame MUST be instantiated on the Swing EDT. The constructor will throw
 * an exception if this is not the case.
 *
 * @param scene a [[Scene]] object representing the model that the view uses.
 * @see [[ScalismoApplication]]
 */
class ScalismoFrame(val scene: Scene) extends MainFrame {

  /**
   * Convenience constructor that instantiates a new Scene instead of requiring one as an argument.
   */
  def this() {
    this(new Scene)
  }

  // some objects, like menu items or actions, want an implicit reference to a ScalismoFrame
  implicit val frame = this

  /**
   * Initializes the frame layout and behavior.
   *
   * @param args command-line arguments
   */
  def setup(args: Array[String]): Unit = {
    setupMenus()
    setupToolbar()
    setupPanels()
    setupBehavior()
  }

  def setupMenus(): Unit = {
    menuBar = new MenuBar

    val fileMenu = new FileMenu
    fileMenu.contents += new ExitItem

    val helpMenu = new HelpMenu
    helpMenu.contents += new AboutItem

    menuBar.contents ++= Seq(fileMenu, helpMenu)
  }

  def setupToolbar(): Unit = {
  }

  /**
   * Sets up the main content.
   * Override this to create an application with a different layout.
   */
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

  // double-check that we're on the correct thread, because if we're not,
  // all hell will break loose in the VTK components.
  require(SwingUtilities.isEventDispatchThread, "ScalismoFrame constructor must be invoked on the Swing EDT!")

  val toolBar = new ToolBar
  val modelPanel = new ModelPanel
  val perspectivePanel = new PerspectivePanel
  val statusBar = new StatusBar

}
