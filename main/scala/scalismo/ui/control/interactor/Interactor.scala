package scalismo.ui.control.interactor

import java.awt.AWTEvent
import java.awt.event.{ KeyEvent, MouseEvent, MouseWheelEvent }

import scalismo.ui.control.interactor.Interactor.Result.Pass
import scalismo.ui.control.interactor.Interactor.{ PimpedEvent, Result }
import scalismo.ui.rendering.internal.GLJPanelWithViewport
import scalismo.ui.view.{ ScalismoFrame, ViewportPanel }

import scala.language.implicitConversions

object Interactor {

  sealed trait Result

  object Result {

    case object Pass extends Result

    case object Block extends Result

  }

  class PimpedEvent[E <: AWTEvent](val event: E) extends AnyVal {
    /** The low-level JComponent where stuff is actually rendered. */
    def canvas: GLJPanelWithViewport = event.getSource.asInstanceOf[GLJPanelWithViewport]

    /** The high-level viewport */
    def viewport: ViewportPanel = canvas.viewport
  }

}

/**
 * Interaction strategy.
 * An interaction strategy gets notified about events before they are passed to the rendering implementation.
 * This can be used for building complicated interaction state machines, as well as to selectively block events
 * from reaching the rendering implementation.
 *
 * Interactors are made active by using the [[ScalismoFrame.interactor]] setter.
 *
 * They are invoked from the [[scalismo.ui.rendering.internal.EventInterceptor]] class, and if the verdict is to
 * pass on the event, it is handled by an [[scalismo.ui.rendering.internal.InteractorForwarder]], ultimately reaching
 * the [[scalismo.ui.rendering.internal.RenderWindowInteractor]] in the rendering implementation.
 *
 * The above paragraph was to explain how things work, but also to provide quick links for navigating to the relevant classes :-)
 *
 */
trait Interactor {
  implicit protected def pimpEvent[E <: AWTEvent](event: E): PimpedEvent[E] = new PimpedEvent(event)

  def onActivated(frame: ScalismoFrame): Unit = {}

  def onDeactivated(frame: ScalismoFrame): Unit = {}

  def keyPressed(e: KeyEvent): Result = Pass

  def keyTyped(e: KeyEvent): Result = Pass

  def mouseMoved(e: MouseEvent): Result = Pass

  def mouseExited(e: MouseEvent): Result = Pass

  def mouseClicked(e: MouseEvent): Result = Pass

  def keyReleased(e: KeyEvent): Result = Pass

  def mouseDragged(e: MouseEvent): Result = Pass

  def mouseEntered(e: MouseEvent): Result = Pass

  def mousePressed(e: MouseEvent): Result = Pass

  def mouseReleased(e: MouseEvent): Result = Pass

  // VTK doesn't handle wheel events anyway, so
  // no need for a return value. But it may be useful for reacting to such events
  def mouseWheelMoved(e: MouseWheelEvent): Unit = {}

}
