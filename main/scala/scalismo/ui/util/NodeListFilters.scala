package scalismo.ui.util

import scalismo.ui.model.SceneNode

import scala.reflect.ClassTag

trait NodeListFilters {
  /**
   * This is a helper method which will filter a list of nodes, and return those which are of a given type T.
   *
   * @param nodes a list of SceneNode
   * @tparam T the type you're interested in
   * @return all the elements in the nodes list which are of type T, as a List[T]
   */
  final def someOf[T: ClassTag](nodes: List[SceneNode]): List[T] = {
    nodes.collect { case n: T => n }
  }

  /**
   * This is a helper method
   * which will return a non-empty list of items of type T,
   * if and only if *all* of the given nodes are of type T.
   *
   * @param nodes a list of SceneNode
   * @tparam T the type you're interested in
   * @return the elements in the nodes list, as a List[T], if *all* of them are of type T, or an empty list otherwise.
   */
  final def allOf[T: ClassTag](nodes: List[SceneNode]): List[T] = {
    val candidates = someOf[T](nodes)
    if (candidates.length == nodes.length) candidates else Nil
  }

  /**
   * This is a helper methode which returns a non-empty Option[T] if and only
   * if the list of nodes consists of a single item of type T
   *
   * @param nodes a list of SceneNode
   * @tparam T the type you're interested in
   * @return Some[T] if the list consisted of a single item of type T, or None otherwise
   */

  final def singleNode[T: ClassTag](nodes: List[SceneNode]): Option[T] = {
    allOf(nodes).headOption
  }

}
