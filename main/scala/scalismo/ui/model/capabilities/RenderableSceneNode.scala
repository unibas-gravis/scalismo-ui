package scalismo.ui.model.capabilities

import scalismo.ui.model.{ Renderable, SceneNode }

trait RenderableSceneNode extends SceneNode with Renderable {

  override def renderables: List[RenderableSceneNode] = {
    this :: super.renderables
  }
}
