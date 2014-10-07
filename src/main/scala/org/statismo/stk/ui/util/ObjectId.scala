package org.statismo.stk.ui.util

object ObjectId {
  def of[T](thing: T) = {
    thing.getClass.getName + "@" + thing.hashCode()
  }
}
