package org.statismo.stk.ui

import scala.swing.event.Event

object Nameable {
  val NoName = "(no name)"

  case class NameChanged(source: Nameable) extends Event

}

trait Nameable extends EdtPublisher {
  private var _name: String = Nameable.NoName

  def name = {
    _name
  }

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
  def defaultGenerator = new AlphaNumericNameGenerator
}

trait NameGenerator {
  def nextName: String
}

object AlphaNumericNameGenerator {
  val Prefixes = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
}

class AlphaNumericNameGenerator extends NameGenerator {

  import AlphaNumericNameGenerator.Prefixes

  private var prefix = 0
  private var suffix = 0

  def nextName = this.synchronized {
    val p = Prefixes(prefix)
    val name = if (suffix == 0) p.toString else s"${p}_$suffix"
    prefix = (prefix + 1) % Prefixes.length()
    if (prefix == 0) suffix += 1

    name
  }
}

class NumericNameGenerator extends NameGenerator {
  private var last = 0

  def nextName = this.synchronized {
    last += 1
    last.toString
  }
}