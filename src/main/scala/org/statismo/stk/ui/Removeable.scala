package org.statismo.stk.ui

import scala.swing.Publisher
import scala.swing.event.Event

object Removeable {
  case class Removed(source: Removeable) extends Event
}

trait Removeable extends EdtPublisher {
  def remove() = {
      publish(Removeable.Removed(this))
  }
  
  def isCurrentlyRemoveable = true
}

trait RemoveableChildren extends Removeable {
  def children: Seq[Removeable]
  override def remove = {
    val copy = children.map{c => c}
    copy.foreach(c => c.remove)
  }
  
  override def isCurrentlyRemoveable = !children.isEmpty
}
