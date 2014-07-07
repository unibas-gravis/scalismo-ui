package org.statismo.stk.ui

import scala.swing.Publisher
import scala.swing.Swing
import scala.swing.event.Event

import javax.swing.SwingUtilities

trait EdtPublisher extends Publisher {
  override def publish(e: Event) = {
    publishEdt(e)
  }

  /* this is the preferred method to use */
  def publishEdt(e: Event) = {
    if (SwingUtilities.isEventDispatchThread) {
      doPublish(e)
    } else {
      Swing.onEDTWait {
        doPublish(e)
      }
    }
  }

  private def doPublish(e: Event) = {
    val copy = listeners.map(l => l)
    copy.foreach {
      l => if (l.isDefinedAt(e)) l(e)
    }
  }
}