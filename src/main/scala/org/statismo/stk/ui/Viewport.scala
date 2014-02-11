package org.statismo.stk.ui

class Viewport(val scene: Scene, initialName: Option[String]) extends Nameable {
  if (initialName.isDefined) name = initialName.get
}