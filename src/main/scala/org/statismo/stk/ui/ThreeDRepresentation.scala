package org.statismo.stk.ui

class ThreeDRepresentations(parent: ThreeDObject) extends SceneTreeObjectContainer[ThreeDRepresentation] with RemoveableChildren {
//  name = "Representations"
//  override lazy val isNameUserModifiable = false
//  override lazy val parent = theObject
  override lazy val publisher = parent
}

trait ThreeDRepresentation extends SceneTreeObject with Removeable {
//  override def parent: ThreeDRepresentations
//  override def parent: ThreeDObject = 
}