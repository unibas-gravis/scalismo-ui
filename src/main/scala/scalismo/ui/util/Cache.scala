/*
 * Copyright (C) 2016  University of Basel, Graphics and Vision Research Group
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scalismo.ui.util

import scala.collection.mutable

object Cache {

  /**
   * Global flag to debug all cache operations.
   */
  //noinspection VarCouldBeVal
  /* This is a global variable that can be set by developers using the library.
   * The "noinspection" comment suppresses a "var could be val" warning from IntelliJ IDEA.
   */
  var DebugAll = false
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
      case None =>
        map.synchronized {
          if (debug) println("cache miss: " + ObjectId.of(key))
          map.getOrElseUpdate(key, new ValueHolder)
        }
    }

    holder.value match {
      case Some(v) => v
      case None =>
        holder.synchronized {
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
