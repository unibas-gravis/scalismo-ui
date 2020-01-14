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

import java.util.{Timer, TimerTask}

object DeferredRendering {

  /**
   * Delay (in milliseconds) before actually rendering. Render requests
   * are grouped and delayed by (at most) this amount of time.
   * The delay should be almost unnoticeable for humans, but the grouping prevents lags
   * that would otherwise be caused by large amounts of render requests arriving at the same time.
   */
  //noinspection VarCouldBeVal
  /* This is a global variable that can be set by developers using the library.
   * The "noinspection" comment suppresses a "var could be val" warning from IntelliJ IDEA.
   */
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

  def request(): Unit = skipped.synchronized {
    if (pending.isEmpty) {
      val task = new DeferredRenderTask
      pending = Some(task)
      super.schedule(task, DeferredRendering.DelayMs)
    } else {
      skipped.count += 1
    }
  }
}
