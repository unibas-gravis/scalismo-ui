package scalismo.ui.model.capabilities

import scalismo.ui.model.SceneNode

trait Removeable extends SceneNode {
  def remove(): Unit
}
