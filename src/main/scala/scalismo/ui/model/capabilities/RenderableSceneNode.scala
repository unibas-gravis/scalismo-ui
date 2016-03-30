package scalismo.ui.model.capabilities

import scalismo.ui.control.NodeVisibility.RenderableNodeWithVisibility
import scalismo.ui.model.{ Renderable, SceneNode }
import scalismo.ui.view.ScalismoFrame

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

  /**
   * Returns a "pimped" (as in "pimp my library") version of this node, which allows to set its visibility in a particular view context (i.e., frame).
   *
   * Strictly speaking, this goes against the convention of a model being completely agnostic of its view. But it's incredibly useful, and since it's an implicit,
   * it can only ever be invoked from a view context anyway.
   *
   * This has to be defined here, because otherwise all view code would have to import an additional implicit method or class.
   *
   */
  implicit def withVisibility(node: RenderableSceneNode)(implicit frame: ScalismoFrame): RenderableNodeWithVisibility = new RenderableNodeWithVisibility(node)(frame)

}