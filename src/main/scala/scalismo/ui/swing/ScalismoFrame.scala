package scalismo.ui.swing

import java.awt.Dimension
import javax.swing.{ SwingUtilities, WindowConstants }

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
    dispose()
  }

  def startup(args: Array[String]): Unit = {
    vtkObjectBase.JAVA_OBJECT_MANAGER.getAutoGarbageCollector.Start()

    size = new Dimension(1024, 768)
    // center on screen
    centerOnScreen()
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
