package org.statismo.stk.ui.swing.props

import scala.swing.BorderPanel


class CombinedPropertiesPanel(override val description: String, delegates: PropertyPanel*) extends BorderPanel with PropertyPanel {
  def setObject(obj: Option[AnyRef]): Boolean = {
    val ok = delegates.map(d => delegatedSetObject(d, obj)).foldLeft(false)({
      (x, y) => x || y
    })
    revalidate()
    ok
  }

  def delegatedSetObject(del: PropertyPanel, obj: Option[AnyRef]): Boolean = {
    val ok = del.setObject(obj)
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