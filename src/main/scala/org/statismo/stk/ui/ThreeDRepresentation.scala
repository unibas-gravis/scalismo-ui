package org.statismo.stk.ui

import org.statismo.stk.ui.visualization.Visualizable

class ThreeDRepresentations(override val publisher: ThreeDObject) extends SceneTreeObjectContainer[ThreeDRepresentation[_]] with RemoveableChildren {
}

trait ThreeDRepresentation[C <: Visualizable[C]] extends SceneTreeObject with Removeable with Visualizable[C] {
  def threeDObject = parent.asInstanceOf[ThreeDObject]
}