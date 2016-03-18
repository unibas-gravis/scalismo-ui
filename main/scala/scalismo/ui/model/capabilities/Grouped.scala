package scalismo.ui.model.capabilities

import scalismo.ui.model.GroupNode

/**
 * This trait simply specifies that the affected node belongs to a group,
 * and defines which group that is.
 */
trait Grouped {
  def group: GroupNode
}
