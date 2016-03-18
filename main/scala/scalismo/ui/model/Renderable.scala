package scalismo.ui.model

/**
 * This is a generic trait for "anything that should
 * be picked up by the renderer". While most renderables
 * will normally directly originate from the scene
 * (like triangle meshes, images etc.), some information
 * (e.g., a bounding box) might not be directly contained
 * in the scene, but should still be visualized. This is
 * why this trait exists.
 */
trait Renderable extends AnyRef {

}
