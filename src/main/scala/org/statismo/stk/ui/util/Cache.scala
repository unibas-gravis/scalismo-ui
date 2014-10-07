package org.statismo.stk.ui.util

import scala.collection.mutable

object Cache {
  final val DebugAll = false
}

class Cache[K, V] {

  private class ValueHolder {
    var value: Option[V] = None
  }

  private val map = new mutable.WeakHashMap[K, ValueHolder]

  def getOrCreate(key: K, op: => V, debug: Boolean = Cache.DebugAll): V = {
    val holder = map.get(key) match {
      case Some(h) =>
        if (debug) println("cache hit: " + ObjectId.of(key))
        h
      case None => map.synchronized {
        if (debug) println("cache miss: " + ObjectId.of(key))
        map.getOrElseUpdate(key, new ValueHolder)
      }
    }

    holder.value match {
      case Some(v) => v
      case None => holder.synchronized {
        holder.value match {
          case Some(v) => v
          case None =>
            holder.value = Some(op)
            holder.value.get
        }
      }
    }
  }
}
