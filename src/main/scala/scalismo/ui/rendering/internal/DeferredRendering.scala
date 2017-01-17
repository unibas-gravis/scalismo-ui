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

package scalismo.ui.rendering.internal

import java.util.{ Timer, TimerTask }

object DeferredRendering {
  // this should be almost unnoticeable for humans, but helps to prevent lags
  // which would be caused by large amounts of render requests arriving at the same time.
  var DelayMs = 25
}

class DeferredRendering(operation: => Unit) extends Timer(true) {

  private class Skipped {
    var count: Int = 0
  }

  private val skipped = new Skipped

  private var pending: Option[DeferredRenderTask] = None

  private class DeferredRenderTask extends TimerTask {
    override def run(): Unit = {
      operation
      skipped.synchronized {
        //println(s"skipped: ${skipped.count}")
        skipped.count = 0
        pending = None
      }
    }
  }

  def request() = skipped.synchronized {
    if (pending.isEmpty) {
      val task = new DeferredRenderTask
      pending = Some(task)
      super.schedule(task, DeferredRendering.DelayMs)
    } else {
      skipped.count += 1
    }
  }
}
