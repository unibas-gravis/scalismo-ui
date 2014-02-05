package org.statismo.stk.ui

import scala.swing.Publisher
import scala.swing.event.Event

object Nameable {
  val DefaultName = ""
  val NoName = "(no name)"
  case class NameChanged(source: Nameable) extends Event
}

trait Nameable extends Publisher {
	private var _name: String = Nameable.DefaultName
	def name = {_name}
	def name_=(newName: String) = {
	  if (newName != _name) {
	    _name = newName
	    publish(Nameable.NameChanged(this))
	  }
	}
	
	lazy val isNameModifiable = true
	override def toString: String = {
	  if (name.trim().length() > 0) name else Nameable.NoName
	}
}