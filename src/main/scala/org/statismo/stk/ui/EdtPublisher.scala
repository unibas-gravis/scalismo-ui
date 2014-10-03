package org.statismo.stk.ui

import org.statismo.stk.ui.util.EdtUtil

import scala.swing.Publisher
import scala.swing.event.Event

trait EdtPublisher extends Publisher {
  override def publish(e: Event) = {
    doPublish(e)
  }

  /* this is the preferred method to use */
  def publishEdt(e: Event) = {
    EdtUtil.onEdt(doPublish(e), wait = true)
  }

  private def doPublish(e: Event) = {
    val copy = listeners.map(l => l)
    copy.foreach {
      l => if (l.isDefinedAt(e)) l(e)
    }
  }
}