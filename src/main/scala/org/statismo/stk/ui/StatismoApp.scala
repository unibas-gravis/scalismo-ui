package org.statismo.stk.ui

import java.awt.Dimension
import scala.Array.canBuildFrom
import scala.swing.MainFrame
import scala.swing.MenuBar
import scala.swing.Reactor
import scala.swing.SimpleSwingApplication
import org.statismo.stk.ui.swing.WorkspacePanel
import javax.swing.UIManager
import javax.swing.WindowConstants
import org.statismo.stk.ui.swing.menu.MainMenuBar
import scala.swing.BorderPanel
import org.statismo.stk.ui.swing.StatismoToolbar

object StatismoApp {
  type FrameConstructor = (Scene => StatismoFrame)

  def defaultFrameConstructor: FrameConstructor = {
    s: Scene => new StatismoFrame(s)
  }

  def apply(args: Array[String] = new Array[String](0), scene: Scene = new Scene, frame: FrameConstructor = defaultFrameConstructor, lookAndFeel: String = defaultLookAndFeelClassName): StatismoApp = {
    UIManager.setLookAndFeel(lookAndFeel)
    val laf = UIManager.getLookAndFeel
    if (laf.getClass.getSimpleName.startsWith("Nimbus")) {
      val defaults = laf.getDefaults
      defaults.put("Tree.drawHorizontalLines", true)
      defaults.put("Tree.drawVerticalLines", true)
    }
    val app = new StatismoApp(frame(scene))
    app.main(args)
    app
  }

  def defaultLookAndFeelClassName: String = {
    val nimbus = UIManager.getInstalledLookAndFeels.filter(_.getName.equalsIgnoreCase("nimbus")).map(i => i.getClassName)
    if (!nimbus.isEmpty) nimbus.head else UIManager.getSystemLookAndFeelClassName
  }

  val Version: String = "0.2.2"
}

class StatismoApp(val top: StatismoFrame) extends SimpleSwingApplication {
  override def startup(args: Array[String]) = {
    top.startup(args)
    super.startup(args)
  }

  def scene = top.scene

}

class StatismoFrame(val scene: Scene) extends MainFrame with Reactor {

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

  lazy val workspace = new Workspace(scene)

  lazy val workspacePanel: WorkspacePanel = new WorkspacePanel(workspace)(this)
  lazy val toolbar: StatismoToolbar = new StatismoToolbar(workspace)

  lazy val mainPanel = new BorderPanel {
    layout(toolbar) = BorderPanel.Position.North
    layout(workspacePanel) = BorderPanel.Position.Center
  }
  contents = mainPanel
  menuBar = new MainMenuBar()(this)

  peer.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

  override def closeOperation() = {
    dispose()
  }

  def startup(args: Array[String]): Unit = {
    size = new Dimension(1024, 768)
    // center on screen
    peer.setLocationRelativeTo(null)
  }

  // this is a hack...
  listenTo(workspace)
  reactions += {
    case Workspace.PleaseLayoutAgain =>
      val d = this.size
      this.size = new Dimension(d.width - 1, this.size.height - 1)
      this.size = d
  }
}

