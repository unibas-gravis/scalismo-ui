package org.statismo.stk.ui.util

object ObjectId {
  def of[T](thing: T) = {
    if (thing == null) "null" else thing.getClass.getName + "@" + thing.hashCode()
  }
}
