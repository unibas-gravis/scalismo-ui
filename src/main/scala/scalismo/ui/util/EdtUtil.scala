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

package scalismo.ui.util

import javax.swing.SwingUtilities

import scala.reflect.ClassTag
import scala.swing.Swing

/**
 * Utility class for performing operations on the Swing Event Dispatch Thread.
 */
object EdtUtil {

  /**
   * Queue an operation for performing it on the Swing EDT.
   *
   * This method may return before the operation has been carried out.
   *
   * @param op operation to perform on EDT
   */
  def onEdt(op: => Unit): Unit = {
    if (SwingUtilities.isEventDispatchThread) {
      op
    } else {
      SwingUtilities.invokeLater(Swing.Runnable(op))
    }
  }

  /**
   * Invoke an operation on the Swing EDT and wait for its result.
   *
   * This method blocks until the operation was performed, and returns the result of the operation.
   *
   * @param op operation to perform on EDT
   * @tparam R result type
   * @return the result of the operation, after it has been carried out on the EDT.
   */
  def onEdtWait[R: ClassTag](op: => R): R = {
    if (SwingUtilities.isEventDispatchThread) {
      op
    } else {
      val result = new Array[R](1)
      SwingUtilities.invokeAndWait(Swing.Runnable {
        result(0) = op
      })
      result(0)
    }
  }
}
