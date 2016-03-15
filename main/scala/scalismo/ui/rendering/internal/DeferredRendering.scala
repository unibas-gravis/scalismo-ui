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
