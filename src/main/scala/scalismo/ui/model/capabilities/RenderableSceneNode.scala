package scalismo.ui.model.capabilities

import scalismo.ui.model.{ Renderable, SceneNode }


/**
 * A renderable node is -- by design -- a leaf node,
 * i.e., it has no further children.
 *
 */
trait RenderableSceneNode extends SceneNode with Renderable {

  final override def renderables: List[RenderableSceneNode] = List(this)

  final override def children: List[SceneNode] = Nil
}

object RenderableSceneNode {

  import scala.language.implicitConversions

}