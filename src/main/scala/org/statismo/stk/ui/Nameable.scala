package org.statismo.stk.ui

import scala.swing.Publisher
import scala.swing.event.Event

object Nameable {
  case class NameChanged(source: Nameable) extends Event
}

trait Nameable extends Publisher {
	private var _name: String = "(no name)"
	def name = {_name}
	def name_=(newName: String) = {
	  if (newName != _name) {
	    _name = newName
	    publish(Nameable.NameChanged(this))
	  }
	}
	
	lazy val isNameModifiable = true
	override def toString: String = name
}