package scalismo.ui.view.properties

import scalismo.ui.model.SceneNode
import scalismo.ui.util.NodeListFilters
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.util.CardPanel

/**
 * A PropertyPanel is a UI component to show or manipulate aspects of
 * [[SceneNode]]s.
 */
trait PropertyPanel extends CardPanel.ComponentWithUniqueId with NodeListFilters {
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
   * It is generally recommended to use the [[allMatch]] method in implementations, which
   * offers a type-safe and convenient way to make decisions about whether a non-empty list of
   * supported nodes was provided.
   *
   * @param nodes list of selected nodes.
   * @return true if this panel can handle the provided nodes, false otherwise.
   */
  def setNodes(nodes: List[SceneNode]): Boolean

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