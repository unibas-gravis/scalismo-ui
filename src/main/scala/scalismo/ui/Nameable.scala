package scalismo.ui

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
      publishEdt(Nameable.NameChanged(this))
    }
  }

  protected[ui] def isNameUserModifiable = true

  override def toString: String = {
    if (name.trim().length() > 0) name else Nameable.NoName
  }
}

object NameGenerator {
  def defaultGenerator = new AlphaNumericNameGenerator
}

trait NameGenerator {
  def nextName: String

  def reset(): Unit
}

trait HasNameGenerator {
  def nameGenerator: NameGenerator
}

object AlphaNumericNameGenerator {
  val Prefixes = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
}

class AlphaNumericNameGenerator extends NameGenerator {

  import scalismo.ui.AlphaNumericNameGenerator.Prefixes

  private var prefix = 0
  private var suffix = 0

  def nextName = {
    val p = Prefixes(prefix)
    val name = if (suffix == 0) p.toString else s"${p}_$suffix"
    prefix = (prefix + 1) % Prefixes.length()
    if (prefix == 0) suffix += 1

    name
  }

  override def reset() = {
    prefix = 0
    suffix = 0
  }
}

class NumericNameGenerator extends NameGenerator {
  private var last = 0

  def nextName = {
    last += 1
    last.toString
  }

  override def reset() = {
    last = 0
  }
}
