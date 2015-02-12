package scalismo.ui.swing

import scalismo.ui.Workspace
import scalismo.ui.swing.props.SceneObjectPropertiesPanel

import scala.swing.BorderPanel.Position.Center
import scala.swing.{ BorderPanel, Orientation, SplitPane }

class PropertiesPanel(val workspace: Workspace) extends BorderPanel {
  val scene = new SceneTreePanel(workspace)
  val details = new SceneObjectPropertiesPanel(workspace)

  setupUi()

  def setupUi() {
    val child = new BorderPanel {
      val split = new SplitPane(Orientation.Horizontal, scene, details) {
        resizeWeight = 0.5
        oneTouchExpandable = true
      }
      // for safety, keep this out of the constructor, to make sure
      // that preferredSize returns something sensible
      split.dividerLocation = (split.preferredSize.height * 0.5).toInt
      layout(split) = Center
    }
    layout(child) = Center
  }
}