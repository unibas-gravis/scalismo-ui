package org.statismo.stk.ui

case class ThreeDRepresentations(theObject: ThreeDObject) extends SceneTreeObjectContainer[ThreeDRepresentation] {
  name = "Representations"
  override lazy val parent = theObject
}

trait ThreeDRepresentation extends Displayable {
}