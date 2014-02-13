package org.statismo.stk.ui

class ThreeDRepresentations(theObject: ThreeDObject) extends SceneTreeObjectContainer[ThreeDRepresentation] with RemoveableChildren {
  name = "Representations"
  override lazy val isNameUserModifiable = false
  override lazy val parent = theObject
}

trait ThreeDRepresentation extends SceneTreeObject with Removeable {
//  override def parent: ThreeDRepresentations
}