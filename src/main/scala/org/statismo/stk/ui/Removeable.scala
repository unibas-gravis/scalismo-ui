package org.statismo.stk.ui

import scala.swing.Publisher
import scala.swing.event.Event

object Removeable {
  case class Removed(source: Removeable) extends Event
}

trait Removeable extends Publisher {
  private var removed = false
  def remove() = {
    if (!removed) {
      removed = true
      publish(Removeable.Removed(this))
    }
  }
}