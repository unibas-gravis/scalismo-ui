package org.statismo.stk.ui.util

import scala.collection.mutable

class Cache[K,V] {

  private class ValueHolder {
    var value: Option[V] = None
  }

  private val map = new mutable.WeakHashMap[K,ValueHolder]

  def getOrCreate(key: K, op: => V): V = {
    val holder = map.get(key) match {
      case Some(h) =>
        //println("cache hit: "+key.getClass + "@"+key.hashCode())
        h
      case None => map.synchronized {
        //println("cache miss: "+key.getClass + "@"+key.hashCode())
        map.getOrElseUpdate(key, new ValueHolder)
      }
    }

    holder.value match {
      case Some(v) => v
      case None => holder.synchronized{
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
