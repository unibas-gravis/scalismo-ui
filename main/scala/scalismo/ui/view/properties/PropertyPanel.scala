package scalismo.ui.view.properties

import scalismo.ui.model.SceneNode
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.util.CardPanel

import scala.reflect.ClassTag

/**
 * A PropertyPanel is a UI component to show or manipulate aspects of
 * [[SceneNode]]s.
 */
trait PropertyPanel extends CardPanel.ComponentWithUniqueId {
  /** human-readable description, used in tabs. */
  def description: String

  // all built-in panels take this as a constructor argument.
  // while not all implementations might need it, this is the "entry point"
  // for the current state (i.e., view and model) of the application.
  def frame: ScalismoFrame

  /**
   * Set the nodes that the user currently selected.
   * Note that the list could be empty.
   *
   * The general contract is that on invocation, an implementation cleans up any current
   * state first, then decides whether it can provide a useful UI for *all* of the nodes.
   * If that is the case, then it should prepare its UI accordingly, and return true. If
   * not, then it simply returns false. In other words: the value that is returned here
   * is used to determine whether this Panel is active (usable) for the current set of
   * selected nodes. If it's not, then it won't be shown, but if it is, then it has to
   * be in a usable state upon return.
   *
   * It is generally recommended to use the [[allOf]] method in implementations, which
   * offers a type-safe and convenient way to make decisions about whether a non-empty list of
   * supported nodes was provided.
   *
   * @param nodes list of selected nodes.
   * @return true if this panel can handle the provided nodes, false otherwise.
   */
  def setNodes(nodes: List[SceneNode]): Boolean

  /**
   * This is a helper method, designed for use in [[setNodes]],
   * which will filter and return the nodes of a given type T.
   *
   * @param nodes a list of SceneNode
   * @tparam T the type you're interested in
   * @return all the elements in the nodes list which are of type T, as a List[T]
   */
  def someOf[T: ClassTag](nodes: List[SceneNode]): List[T] = {
    nodes.collect { case n: T => n }
  }

  /**
   * This is a helper method, designed for use in [[setNodes]],
   * which will return a non-empty list of items of type T,
   * if and only if *all* of the given nodes are of type T.
   *
   * @param nodes a list of SceneNode
   * @tparam T the type you're interested in
   * @return the elements in the nodes list, as a List[T], if *all* of them are of type T, or an empty list otherwise.
   */
  def allOf[T: ClassTag](nodes: List[SceneNode]): List[T] = {
    val candidates = someOf[T](nodes)
    if (candidates.length == nodes.length) candidates else Nil
  }

  override def toString(): String = description

}

object PropertyPanel {

  trait Factory {
    def create(frame: ScalismoFrame): PropertyPanel
  }

  object Factory {

    import scala.language.implicitConversions

    implicit def factoryAsConstructor(factory: Factory): (ScalismoFrame => PropertyPanel) = {
      factory.create
    }
  }

}