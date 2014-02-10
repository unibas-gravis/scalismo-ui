package org.statismo.stk.ui

import scala.swing.Publisher
import scala.swing.event.Event
import scala.swing.Swing
import javax.swing.SwingUtilities

trait EdtPublisher extends Publisher {
	override def publish(e: Event) = {
	  if (SwingUtilities.isEventDispatchThread()) {
	    super.publish(e)
	  } else {
	    Swing.onEDT(super.publish(e))
	  }
	}
}