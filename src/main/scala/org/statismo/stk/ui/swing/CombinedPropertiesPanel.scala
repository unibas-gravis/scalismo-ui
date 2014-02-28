package org.statismo.stk.ui.swing

import scala.swing.BorderPanel

import org.statismo.stk.ui.SceneTreeObject

class CombinedPropertiesPanel(override val description: String, delegates: SceneObjectPropertyPanel*) extends BorderPanel with SceneObjectPropertyPanel {
  def setObject(obj: Option[SceneTreeObject]): Boolean = {
    val ok = delegates.map(d => delegatedSetObject(d, obj)).foldLeft(false)({ (x, y) => x || y })
    revalidate
    ok
  }

  def delegatedSetObject(del: SceneObjectPropertyPanel, obj: Option[SceneTreeObject]): Boolean = {
    val ok = del.setObject(obj)
    del.visible = ok
    ok
  }

  val panel: BorderPanel = CombinedPropertiesPanel.this

  delegates.reverse.foldLeft(panel)({ (panel, comp) =>
    val child = new BorderPanel
    child layout comp = BorderPanel.Position.Center
    panel layout child = BorderPanel.Position.North
    child
  })
}