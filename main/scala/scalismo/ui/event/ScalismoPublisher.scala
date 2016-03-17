package scalismo.ui.event

import com.github.ghik.silencer.silent
import scalismo.ui.util.EdtUtil

import scala.swing.Publisher

/**
 * Provides a method for publishing events on the correct thread.
 *
 * To ensure that events
 * are published on the Swing Event Dispatch Thread, use the
 * publishEdt method.
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
  override def publish(e: Event) = {
    doPublish(e)
  }

  // this is the preferred method to use
  def publishEvent(e: Event) = {
    EdtUtil.onEdtWait(doPublish(e))
  }

  private def doPublish(e: Event) = {
    // make sure that each listener is notified, even if the
    // listeners change during the handling.
    val copy = listeners.map(l => l)
    copy.foreach {
      l => if (l.isDefinedAt(e)) l(e)
    }
  }
}
