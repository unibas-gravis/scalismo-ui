package scalismo.ui.swing

import java.awt.Dimension
import java.util.concurrent.TimeUnit
import javax.swing.{ SwingUtilities, WindowConstants }

import scalismo.ui.settings.PersistentSettings
import scalismo.ui.swing.menu.MainMenuBar
import scalismo.ui.util.EdtUtil
import scalismo.ui.{ Scene, Workspace }
import vtk.vtkObjectBase

import scala.swing._

object ScalismoFrame {
  type FrameConstructor = (Scene => ScalismoFrame)

  def apply(constructor: FrameConstructor, scene: Scene = new Scene): ScalismoFrame = {
    var result: Option[ScalismoFrame] = None
    EdtUtil.onEdt({
      val theFrame = constructor(scene)
      result = Some(theFrame)
    }, wait = true)
    result.get
  }
}

class ScalismoFrame(val scene: Scene) extends MainFrame with Reactor {
  if (!SwingUtilities.isEventDispatchThread) {
    sys.error("ScalismoFrame constructor must be invoked in the Swing EDT. See ScalismoFrame.apply()")
  }

  title = "Scalismo Viewer"

  override def menuBar: MainMenuBar = {
    super.menuBar.asInstanceOf[MainMenuBar]
  }

  override def menuBar_=(mb: MenuBar) = {
    if (mb.isInstanceOf[MainMenuBar]) {
      super.menuBar_=(mb)
    } else {
      throw new RuntimeException("MenuBar must be of type scalismo.ui.swing.menu.MainMenuBar")
    }
  }

  lazy val console = new Console()(this)

  lazy val workspace = new Workspace(scene)
  lazy val workspacePanel: TunableWorkspacePanel = new TunableWorkspacePanel(workspace)

  def toolbar: ScalismoToolbar = workspacePanel.toolbar

  lazy val mainPanel: Component = new BorderPanel {
    layout(workspacePanel) = BorderPanel.Position.Center
  }

  contents = mainPanel
  menuBar = new MainMenuBar()(this)

  peer.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

  override def closeOperation() = {
    saveWindowState()
    dispose()
  }

  def saveWindowState(): Unit = {
    scene.imageWindowLevel.save()
    val dim = this.size
    PersistentSettings.set(PersistentSettings.Keys.WindowMaximized, this.maximized)
    if (!maximized) {
      // these settings are only saved if not maximized. However, if the window *is* maximized, they should have been saved before, in the call to maximize()
      PersistentSettings.set(PersistentSettings.Keys.WindowWidth, dim.width)
      PersistentSettings.set(PersistentSettings.Keys.WindowHeight, dim.height)
    }
  }

  override def maximize(): Unit = {
    // we need to store the settings in the "unmaximized" state
    val dim = this.size
    PersistentSettings.set(PersistentSettings.Keys.WindowWidth, dim.width)
    PersistentSettings.set(PersistentSettings.Keys.WindowHeight, dim.height)
    super.maximize()
  }

  def restoreWindowState(): Unit = {
    val width = PersistentSettings.get[Int](PersistentSettings.Keys.WindowWidth).getOrElse(1024)
    val height = PersistentSettings.get[Int](PersistentSettings.Keys.WindowHeight).getOrElse(768)
    size = new Dimension(width, height)
    centerOnScreen()

    val max = PersistentSettings.get[Boolean](PersistentSettings.Keys.WindowMaximized).getOrElse(false)
    if (max) {
      maximize()
    }
  }

  // This is a def so that the interval can be easily overridden. The unit is in seconds.
  def garbageCollectorInterval: Int = 60

  // This is *required*, otherwise you'll run out of memory sooner or later. The current rendering engine (VTK)
  // uses native objects which have their own life cycles and must be periodically garbage-collected.
  def startGarbageCollector(): Unit = {
    vtkObjectBase.JAVA_OBJECT_MANAGER.getAutoGarbageCollector.SetScheduleTime(garbageCollectorInterval, TimeUnit.SECONDS)
    vtkObjectBase.JAVA_OBJECT_MANAGER.getAutoGarbageCollector.Start()
  }

  // Make sure that you are calling super.startup() when overriding, or alternatively "manually" call startGarbageCollector()!
  def startup(args: Array[String]): Unit = {
    restoreWindowState()
    startGarbageCollector()
  }

  // this is a hack...
  listenTo(workspace)
  reactions += {
    case Workspace.PleaseLayoutAgain(ws) =>
      val d = this.size
      this.size = new Dimension(d.width + 1, this.size.height + 1)
      this.size = d
  }

  override def dispose() = {
    console.dispose()
    scene.viewports.foreach(_.destroy())
    vtkObjectBase.JAVA_OBJECT_MANAGER.getAutoGarbageCollector.Stop()
    super.dispose()
  }
}
