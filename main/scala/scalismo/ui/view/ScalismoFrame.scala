package scalismo.ui.view

import java.awt.Dimension
import javax.swing.{ SwingUtilities, WindowConstants }

import scalismo.ui.event.{ Event, ScalismoPublisher }
import scalismo.ui.model.{ Scene, SceneNode }
import scalismo.ui.settings.GlobalSettings
import scalismo.ui.view.ScalismoFrame.event.SelectedNodesChanged
import scalismo.ui.view.menu.FileMenu.ExitItem
import scalismo.ui.view.menu.HelpMenu.AboutItem
import scalismo.ui.view.menu.ViewMenu.{ HighDpiSetScaleItem, PerspectiveMenu }
import scalismo.ui.view.menu.{ FileMenu, HelpMenu, ViewMenu }

import scala.swing.{ BorderPanel, MainFrame, MenuBar }

object ScalismoFrame {

  object event {

    case class SelectedNodesChanged(frame: ScalismoFrame) extends Event

  }

}

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
class ScalismoFrame(val scene: Scene) extends MainFrame with ScalismoPublisher {

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

    val viewMenu = new ViewMenu
    viewMenu.contents ++= Seq(new PerspectiveMenu, new HighDpiSetScaleItem)

    menuBar.contents ++= Seq(fileMenu, viewMenu, helpMenu)
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
    root.layout(perspectivesPanel) = BorderPanel.Position.Center

    this.contents = root
  }

  def setupBehavior(): Unit = {
    peer.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
    restoreWindowState()
  }

  override def closeOperation(): Unit = {
    saveWindowState()
    dispose()
  }

  protected def saveWindowWidthHeight(): Unit = {
    val dim = this.size
    GlobalSettings.set(GlobalSettings.Keys.WindowWidth, dim.width)
    GlobalSettings.set(GlobalSettings.Keys.WindowHeight, dim.height)
  }

  override def maximize(): Unit = {
    // we need to store the width/height in the "unmaximized" state, before we actually go maximized.
    saveWindowWidthHeight()
    super.maximize()
  }

  protected def saveWindowState(): Unit = {
    GlobalSettings.set(GlobalSettings.Keys.WindowMaximized, maximized)
    if (!maximized) {
      // Saving width/height in maximized state is pointless. It should have been done *before*
      // the window was maximized (see the maximize() method)
      saveWindowWidthHeight()
    }
  }

  protected def restoreWindowState(): Unit = {
    val width = GlobalSettings.get[Int](GlobalSettings.Keys.WindowWidth).getOrElse(1024)
    val height = GlobalSettings.get[Int](GlobalSettings.Keys.WindowHeight).getOrElse(768)
    size = new Dimension(width, height)
    centerOnScreen()

    if (GlobalSettings.get[Boolean](GlobalSettings.Keys.WindowMaximized).getOrElse(false)) {
      maximize()
    }
  }

  private var _selectedNodes: List[SceneNode] = Nil

  def selectedNodes = _selectedNodes

  def selectedNodes_=(nodes: List[SceneNode]) = {
    if (_selectedNodes != nodes) {
      _selectedNodes = nodes
      publishEvent(SelectedNodesChanged(this))
    }
  }

  // double-check that we're on the correct thread, because if we're not,
  // all hell will break loose in the VTK components.
  require(SwingUtilities.isEventDispatchThread, "ScalismoFrame constructor must be invoked on the Swing EDT!")

  val toolBar = new ToolBar
  val modelPanel = new ModelPanel(frame)
  val perspectivesPanel: PerspectivesPanel = new PerspectivesPanel(frame)
  val statusBar = new StatusBar

}
