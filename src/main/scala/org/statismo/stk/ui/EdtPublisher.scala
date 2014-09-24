package org.statismo.stk.ui

import org.statismo.stk.ui.util.EdtUtil

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
    doPublish(e)
  }

  private def doPublish(e: Event) = {
    val copy = listeners.map(l => l)
    copy.foreach {
      l =>
        new Thread() {
        override def run() = {
          if (l.isDefinedAt(e)) {
            EdtUtil.onEdt(l(e), wait = false)
          }
        }
      }.start()
    }
  }
}