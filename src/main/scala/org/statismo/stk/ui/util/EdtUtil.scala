package org.statismo.stk.ui.util

import javax.swing.SwingUtilities

import scala.swing.Swing

object EdtUtil {
  def onEdt(op: => Unit, wait: Boolean = false): Unit = {
    if (SwingUtilities.isEventDispatchThread) {
      op
    } else {
      if (wait) {
        Swing.onEDTWait(op)
      } else {
        Swing.onEDT(op)
      }
    }
  }
}
