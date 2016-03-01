package scalismo.ui.model.capabilities

import scalismo.ui.model.SceneNode

/**
 * This trait is relevant only for the presentation of nodes
 * in the tree view. If the isViewCollapsed method returns true,
 * then the node will not show up in the tree (at all), but instead
 * its (non-collapsed) children are directly added to this node's parent.
 */
trait CollapsableView extends SceneNode {
  def isViewCollapsed: Boolean
}
