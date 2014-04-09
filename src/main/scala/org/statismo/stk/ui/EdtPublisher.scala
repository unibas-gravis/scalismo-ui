package org.statismo.stk.ui

import scala.swing.Publisher
import scala.swing.Swing
import scala.swing.event.Event

import javax.swing.SwingUtilities

trait EdtPublisher extends Publisher {
  override def publish(e: Event) = {
    publishLater(e)
//    if (SwingUtilities.isEventDispatchThread) {
//      println(s"$this published directly")
//      super.publish(e)
//    } else {
//      Swing.onEDTWait {
//        println("published indirectly")
//        super.publish(e)
//      }
//    }
  }

  def publishLater(e: Event) = {
    def doPublish(e: Event) = {
      e match {
        case Removeable.Removed(s) =>
          println(s"removed ($s) is handled by ${listeners.size} listeners")
        case _ =>
      }
      //println("actually publishing "+e.getClass + " to "+listeners.size+ " listeners")
      super.publish(e)
    }
    new Thread() {
      override def run() = {
        Swing.onEDTWait(doPublish(e))
      }
    }.start()
  }
}