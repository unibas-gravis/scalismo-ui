package scalismo.ui.view

import javax.swing.{ SwingUtilities, WindowConstants }

import scalismo.ui.model.Scene
import scalismo.ui.util.EdtUtil
import scalismo.ui.view.menu.FileMenu
import scalismo.ui.view.menu.FileMenu.ExitItem

import scala.swing.{ BorderPanel, MainFrame, MenuBar }

object ScalismoFrame {
  /**
   * Type alias for a method which takes a [[Scene]], and produces a [[ScalismoFrame]] (or a subclass thereof)
   */
  type Constructor = (Scene => ScalismoFrame)

  val DefaultConstructor: Constructor = {
    s: Scene => new ScalismoFrame(s)
  }

  /**
   * Creates a [[ScalismoFrame]] (or a subclass thereof) given a [[Scene]] and a constructor method.
   *
   * This method takes care of invoking the constructor in the correct thread (EDT).
   * @param scene the scene. If not specified, a new Scene is created.
   * @param constructor the constructor method. If not specified, the [[DefaultConstructor]] is used.
   * @return
   */
  def apply(scene: Scene = new Scene, constructor: Constructor = DefaultConstructor): ScalismoFrame = {
    EdtUtil.onEdtWait(constructor(scene))
  }
}

/**
 * A ScalismoFrame is the top-level view object of every application using the Scalismo UI.
 * @param scene a [[Scene]] object representing the model that the view uses.
 *
 * @see [[ScalismoApplication]]
 */
class ScalismoFrame protected (val scene: Scene) extends MainFrame {

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

    menuBar.contents += fileMenu
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
