package scalismo.ui.view

import scala.swing.{ SplitPane, Orientation, BorderPanel }

class ModelPanel(val frame: ScalismoFrame) extends BorderPanel {

  def setupPanels(): Unit = {
    val split = new SplitPane(Orientation.Horizontal, nodesPanel, propertiesPanel) {
      resizeWeight = 0.5
      oneTouchExpandable = true
    }
    // for safety, keep this out of the constructor, to make sure
    // that preferredSize returns something sensible
    split.dividerLocation = (split.preferredSize.height * 0.5).toInt
    layout(split) = BorderPanel.Position.Center
  }

  val nodesPanel = new NodesPanel(frame)
  val propertiesPanel = new NodePropertiesPanel(frame)

  setupPanels()
}
