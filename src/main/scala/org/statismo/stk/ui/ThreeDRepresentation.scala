package org.statismo.stk.ui

class ThreeDRepresentations(theObject: ThreeDObject) extends SceneTreeObjectContainer[ThreeDRepresentation] {
  name = "Representations"
  override lazy val isNameUserModifiable = false
  override lazy val parent = theObject
}

trait ThreeDRepresentation extends Displayable {
}