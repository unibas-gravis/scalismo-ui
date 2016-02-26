package scalismo.ui.util

import javax.swing.SwingUtilities

import scala.reflect.ClassTag
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

  def onEdtWithResult[R: ClassTag](op: => R): R = {
    if (SwingUtilities.isEventDispatchThread) {
      op
    } else {
      val result = new Array[R](1)
      Swing.onEDTWait {
        result(0) = op
      }
      result(0)
    }
  }
}
