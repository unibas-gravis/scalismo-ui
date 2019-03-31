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

package scalismo.ui.event

import com.github.ghik.silencer.silent
import scalismo.ui.util.EdtUtil

import scala.swing.Publisher

/**
 * Provides a method for publishing events on the correct thread.
 *
 * To ensure that events
 * are published on the Swing Event Dispatch Thread, use the
 * publishEvent method.
 */
trait ScalismoPublisher extends Publisher {

  /* We need to ensure that we are a Publisher, but then again, we also
   * have to discourage people from using the publish() method, because
   * that one assumes to already be on the EDT. That's why it's tagged
   * as deprecated (it's not actually deprecated, but this seems to be
   * the only reliable way to show a warning when used).
   *
   * NOTE: this in turn results in a "... overrides concrete,
   * non-deprecated ..." warning, but that one can be safely IGNORED.
   *
   * NOTE-2: The warnings mentioned above have been silenced using
   * https://github.com/ghik/silencer -- for this class only.
   * Thus, the UI should compile without warnings, but you will still
   * get a warning if you try to use the publish() method. Sweet! :-)
   */
  @deprecated(message = "use method publishEvent instead", since = "always")
  @silent
  override def publish(e: Event): Unit = {
    doPublish(e)
  }

  // this is the preferred method to use
  def publishEvent(e: Event): Unit = {
    EdtUtil.onEdtWait(doPublish(e))
  }

  private def doPublish(e: Event): Unit = {
    // make sure that each listener is notified, even if the
    // listeners change during the handling.
    val copy = listeners.map(l => l)
    copy.foreach {
      l => if (l.isDefinedAt(e)) l(e)
    }
  }
}
