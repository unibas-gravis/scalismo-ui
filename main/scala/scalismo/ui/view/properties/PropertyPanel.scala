package scalismo.ui.view.properties

import scalismo.ui.model.SceneNode
import scalismo.ui.view.{ ScalismoFrame, CardPanel }

trait PropertyPanel extends CardPanel.ComponentWithUniqueId {
  def description: String

  def frame: ScalismoFrame

  def setNodes(nodes: List[SceneNode]): Boolean

  override def toString(): String = description

}

