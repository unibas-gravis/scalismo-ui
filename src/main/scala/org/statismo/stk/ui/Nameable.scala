package org.statismo.stk.ui

import scala.swing.Publisher
import scala.swing.event.Event

object Nameable {
  val DefaultName = ""
  val NoName = "(no name)"
  case class NameChanged(source: Nameable) extends Event
}

trait Nameable extends EdtPublisher {
	private var _name: String = Nameable.DefaultName
	def name = {_name}
	def name_=(newName: String) = {
	  if (newName != _name) {
	    _name = newName
	    publish(Nameable.NameChanged(this))
	  }
	}
	
	def isNameUserModifiable = true
	override def toString: String = {
	  if (name.trim().length() > 0) name else Nameable.NoName
	}
}

object NameGenerator {
  def defaultGenerator = new NumberNameGenerator
}

trait NameGenerator {
  def nextName: String
}

class NumberNameGenerator extends NameGenerator {
  private var last = 0
  
  def nextName = this.synchronized {
    last += 1
    last.toString
  }
}