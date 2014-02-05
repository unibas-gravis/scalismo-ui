package org.statismo.stk.ui

import scala.swing.Publisher
import scala.swing.event.Event

object Displayable {
  case class VisibilityChanged(source: Displayable) extends Event
}

trait Displayable extends SceneTreeObject {
	private var _visible: Boolean = true
	
	def visible = _visible
	def visible_=(newVisible:Boolean) = {
	  if (newVisible != _visible) {
	    _visible = newVisible
	    publish(Displayable.VisibilityChanged(this))
	  }
	}
}