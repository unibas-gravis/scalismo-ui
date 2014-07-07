package org.statismo.stk.ui

import _root_.vtk.vtkObjectBase
import scala.Array.canBuildFrom
import scala.swing._
import org.statismo.stk.ui.swing._
import javax.swing.{SwingUtilities, UIManager, WindowConstants}
import org.statismo.stk.ui.swing.menu.MainMenuBar

object StatismoApp {

  import StatismoFrame.FrameConstructor

  def defaultFrameConstructor: FrameConstructor = {
    s: Scene => new StatismoFrame(s)
  }

  def apply(args: Array[String] = new Array[String](0), scene: Scene = new Scene, frame: FrameConstructor = defaultFrameConstructor, lookAndFeelClassName: String = defaultLookAndFeelClassName): StatismoApp = {
    StatismoLookAndFeel.initializeWith(lookAndFeelClassName)
    val app = new StatismoApp(StatismoFrame(frame, scene))
    app.main(args)
    app
  }

  def defaultLookAndFeelClassName: String = {
    val nimbus = UIManager.getInstalledLookAndFeels.filter(_.getName.equalsIgnoreCase("nimbus")).map(i => i.getClassName)
    if (!nimbus.isEmpty) nimbus.head else UIManager.getSystemLookAndFeelClassName
  }
}

class StatismoApp(val top: StatismoFrame) extends SimpleSwingApplication {
  override def startup(args: Array[String]) = {
    top.startup(args)
    super.startup(args)
  }

  def scene = top.scene

}

object StatismoFrame {
  type FrameConstructor = (Scene => StatismoFrame)

  def apply(constructor: FrameConstructor, scene: Scene = new Scene): StatismoFrame = {
    var result: Option[StatismoFrame] = None
    Swing.onEDTWait {
      val theFrame = constructor(scene)
      result = Some(theFrame)
    }
    result.get
  }
}

class StatismoFrame(val scene: Scene) extends MainFrame with Reactor {
  if (!SwingUtilities.isEventDispatchThread) {
    sys.error("StatismoFrame constructor must be invoked in the Swing EDT. See StatismoFrame.apply()")
  }

  title = "Statismo Viewer"

  override def menuBar: MainMenuBar = {
    super.menuBar.asInstanceOf[MainMenuBar]
  }

  override def menuBar_=(mb: MenuBar) = {
    if (mb.isInstanceOf[MainMenuBar]) {
      super.menuBar_=(mb)
    } else {
      throw new RuntimeException("MenuBar must be of type org.statismo.stk.ui.swing.menu.MainMenuBar")
    }
  }

  lazy val console = new Console()(this)

  lazy val workspace = new Workspace(scene)
  private lazy val workspacePanel: WorkspacePanel = new WorkspacePanel(workspace)

  def toolbar: StatismoToolbar = workspacePanel.toolbar

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
    vtkObjectBase.JAVA_OBJECT_MANAGER.getAutoGarbageCollector().Stop()
    super.dispose()
  }
}

