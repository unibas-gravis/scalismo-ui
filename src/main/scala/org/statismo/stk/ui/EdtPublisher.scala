package org.statismo.stk.ui

import scala.swing.Publisher
import scala.swing.Swing
import scala.swing.event.Event

import javax.swing.SwingUtilities

trait EdtPublisher extends Publisher {
  override def publish(e: Event) = {
    if (SwingUtilities.isEventDispatchThread) {
      doPublish(e)
    } else {
      Swing.onEDT {
        doPublish(e)
      }
    }
  }

  private def doPublish(e: Event) = {
    val copy = listeners.map(l => l)
    copy.foreach {l => if (l.isDefinedAt(e)) l(e) }
  }
}