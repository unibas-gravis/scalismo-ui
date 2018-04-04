/*
 * Copyright (C) 2016  University of Basel, Graphics and Vision Research Group 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scalismo.ui.view

import java.awt.Dimension

import javax.swing.{ SwingUtilities, WindowConstants }
import scalismo.ui.control.SceneControl
import scalismo.ui.control.interactor.landmark.simple.SimpleLandmarkingInteractor
import scalismo.ui.view.menu.ViewMenu.ShowBackgroundColorDialogItem

import scalismo.ui.control.interactor.Interactor
import scalismo.ui.event.{ Event, ScalismoPublisher }
import scalismo.ui.model.{ Scene, SceneNode }
import scalismo.ui.settings.GlobalSettings
import scalismo.ui.view.ScalismoFrame.event.SelectedNodesChanged
import scalismo.ui.view.menu.FileMenu.CloseFrameItem
import scalismo.ui.view.menu.HelpMenu.ShowAboutDialogItem
import scalismo.ui.view.menu.ViewMenu.{ PerspectiveMenu, ShowDisplayScalingDialogItem }
import scalismo.ui.view.menu.{ FileMenu, HelpMenu, ViewMenu }

import scala.swing.{ BorderPanel, Component, MainFrame, MenuBar }

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
  implicit val frame: ScalismoFrame = this

  /* This is the component that is returned as the parent component when creating a dialog.
   * Normally, this should not be null. However, it seems like there is a bug on Linux where
   * dialogs sometimes show up empty if this value is NOT null, so for now we return null
  */
  def componentForDialogs: Component = {
    contents.headOption.orNull
    //null
  }

  val sceneControl: SceneControl = new SceneControl(this, scene)

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
    fileMenu.contents += new CloseFrameItem

    val helpMenu = new HelpMenu
    helpMenu.contents += new ShowAboutDialogItem

    val viewMenu = new ViewMenu
    viewMenu.contents ++= Seq(new PerspectiveMenu, new ShowDisplayScalingDialogItem, new ShowBackgroundColorDialogItem)

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
    root.layout(toolbar) = BorderPanel.Position.North
    root.layout(modelPanel) = BorderPanel.Position.West
    root.layout(status) = BorderPanel.Position.South
    root.layout(perspective) = BorderPanel.Position.Center

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

  override def dispose(): Unit = {
    super.dispose()
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

  def selectedNodes: List[SceneNode] = _selectedNodes

  def selectedNodes_=(nodes: List[SceneNode]): Unit = {
    if (_selectedNodes != nodes) {
      _selectedNodes = nodes
      publishEvent(SelectedNodesChanged(this))
    }
  }

  private var _interactor: Interactor = SimpleLandmarkingInteractor

  def interactor: Interactor = _interactor

  def interactor_=(newInteractor: Interactor): Unit = {
    if (newInteractor != interactor) {
      interactor.onDeactivated(this)
      _interactor = newInteractor
      newInteractor.onActivated(this)
    }
  }

  // constructor logic

  title = "Scalismo UI"

  // double-check that we're on the correct thread, because if we're not,
  // all hell will break loose in the VTK components.
  require(SwingUtilities.isEventDispatchThread, "ScalismoFrame constructor must be invoked on the Swing EDT!")

  val toolbar = new ToolBar
  val modelPanel = new ModelPanel(this)
  val perspective: PerspectivePanel = new PerspectivePanel(this)
  val status = new StatusBar

  // the controls can only be initialized once the frame is fully constructed,
  sceneControl.initialize()
  interactor.onActivated(this)
}
