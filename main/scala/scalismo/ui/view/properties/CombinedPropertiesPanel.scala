package scalismo.ui.view.properties

import scalismo.ui.model.SceneNode
import scalismo.ui.view.ScalismoFrame

import scala.swing.BorderPanel

/**
 * This class combines multiple property panels into a single one.
 * The resulting layout is similar to a (vertical) BoxLayout,
 * except that each component only takes as much space as it
 * actually needs. This is achieved by nesting BorderPanels.
 *
 * @param frame top-level frame
 * @param description human-readable name
 * @param delegates property panels to be combined.
 */
class CombinedPropertiesPanel(override val frame: ScalismoFrame, override val description: String, delegates: PropertyPanel*) extends BorderPanel with PropertyPanel {

  /*
  * This will return true if *any* of the delegates returns true,
  * and false only if *no* delegate returns true.
  * In other words: The panel is functional if a non-empty subset
  * of its delegates is functional. Non-functional delegates are hidden.
  */
  override def setNodes(nodes: List[SceneNode]): Boolean = {
    val ok = delegates.map(d => delegatedSetNodes(d, nodes)).foldLeft(false)({
      (x, y) => x || y
    })
    revalidate()
    ok
  }

  private def delegatedSetNodes(del: PropertyPanel, nodes: List[SceneNode]): Boolean = {
    val ok = del.setNodes(nodes)
    del.visible = ok
    ok
  }

  // constructor

  delegates.reverse.foldLeft(this: BorderPanel)({
    (panel, comp) =>
      val child = new BorderPanel
      child layout comp = BorderPanel.Position.Center
      panel layout child = BorderPanel.Position.North
      child
  })
}
