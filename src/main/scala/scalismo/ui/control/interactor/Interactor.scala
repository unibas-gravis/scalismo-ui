package scalismo.ui.control.interactor

import java.awt.event.{ InputEvent, KeyEvent, MouseEvent, MouseWheelEvent }

import scalismo.ui.control.interactor.Interactor.Verdict.Pass
import scalismo.ui.control.interactor.Interactor.{ PimpedEvent, Verdict }
import scalismo.ui.rendering.internal.GLJPanelWithViewport
import scalismo.ui.view.{ ScalismoFrame, ViewportPanel }

import scala.language.implicitConversions

object Interactor {

  sealed trait Verdict

  object Verdict {

    case object Pass extends Verdict

    case object Block extends Verdict

  }

  class PimpedEvent[E <: InputEvent](val event: E) extends AnyVal {
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
 *
 * The UI comes with a couple of predefined recipes, defined in the [[Recipe]] class, as well as traits which combine recipes
 * into fully-usable "behavior bundles". When mixing in these traits, the order may be important. Roughly speaking, traits
 * further to the right take effect first. When you call a method on a class with mixins, the method in the trait furthest
 * to the right is called first. If that method calls super, it invokes the method in the next trait to its left, and so on.
 *
 * Mixins are useful, but have to be handled with care. Don't overdo it. If in doubt, manually combine recipes instead.
 *
 */
trait Interactor {
  implicit protected def pimpEvent[E <: InputEvent](event: E): PimpedEvent[E] = new PimpedEvent(event)

  /**
   * This method is invoked when an interactor is
   * activated in a frame.
   *
   * It can be used to initialize the interactor state,
   * or to add UI elements (e.g. toolbar buttons)
   * to the frame.
   *
   */
  def onActivated(frame: ScalismoFrame): Unit = {}

  /**
   * This method is invoked when an interactor is
   * deactivated, i.e., removed.
   *
   * It should clean up / revert any
   * changes that the interactor made to the UI.
   *
   */
  def onDeactivated(frame: ScalismoFrame): Unit = {}

  def keyPressed(e: KeyEvent): Verdict = Pass

  def keyTyped(e: KeyEvent): Verdict = Pass

  def mouseMoved(e: MouseEvent): Verdict = Pass

  def mouseExited(e: MouseEvent): Verdict = Pass

  def mouseClicked(e: MouseEvent): Verdict = Pass

  def keyReleased(e: KeyEvent): Verdict = Pass

  def mouseDragged(e: MouseEvent): Verdict = Pass

  def mouseEntered(e: MouseEvent): Verdict = Pass

  def mousePressed(e: MouseEvent): Verdict = Pass

  def mouseReleased(e: MouseEvent): Verdict = Pass

  /* The rendering implementation (VTK) does not
   * handle scroll events, but we still want a
   * return value in the case of delegations
   */
  def mouseWheelMoved(e: MouseWheelEvent): Verdict = Pass

}
