package org.statismo.stk.ui

class ThreeDRepresentations(override val publisher: ThreeDObject) extends SceneTreeObjectContainer[ThreeDRepresentation] with RemoveableChildren {
}

trait ThreeDRepresentation extends SceneTreeObject with Removeable {
  def threeDObject = parent.asInstanceOf[ThreeDObject]
}