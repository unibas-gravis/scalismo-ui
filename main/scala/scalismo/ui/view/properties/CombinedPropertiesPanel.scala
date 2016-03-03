package scalismo.ui.view.properties

import scalismo.ui.model.SceneNode
import scalismo.ui.view.ScalismoFrame

import scala.swing.BorderPanel

class CombinedPropertiesPanel(override val frame: ScalismoFrame, override val description: String, delegates: PropertyPanel*) extends BorderPanel with PropertyPanel {

  override def setNodes(nodes: List[SceneNode]): Boolean = {
    val ok = delegates.map(d => delegatedSetNodes(d, nodes)).foldLeft(false)({
      (x, y) => x || y
    })
    revalidate()
    ok
  }

  def delegatedSetNodes(del: PropertyPanel, nodes: List[SceneNode]): Boolean = {
    val ok = del.setNodes(nodes)
    del.visible = ok
    ok
  }

  val panel: BorderPanel = CombinedPropertiesPanel.this

  delegates.reverse.foldLeft(panel)({
    (panel, comp) =>
      val child = new BorderPanel
      child layout comp = BorderPanel.Position.Center
      panel layout child = BorderPanel.Position.North
      child
  })
}
