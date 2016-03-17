package scalismo.ui.control.interactor

import java.awt.AWTEvent
import java.awt.event.{MouseEvent, MouseWheelEvent}

import scalismo.ui.control.interactor.Interactor.Verdict.{Pass, Block}
import scalismo.ui.control.interactor.Interactor.{Verdict, PimpedEvent}
import scalismo.ui.view.ViewportPanel2D

import scala.language.implicitConversions

/**
  * This object provides recipes for Interactor implementations.
  *
  * Think of it as a "utils" class that provides commonly needed
  * functionality.
  *
  * An alternative implementation would have been to use traits
  * which override specific methods, but that would quickly get
  * out of hand when combinations of functionality are needed.
  *
  * The objects defined here are all named after their purpose,
  * and the methods they provide mimick the methods found in
  * the Interactor trait.
  */
object Recipe {
  implicit def pimpEvent[E <: AWTEvent](event: E): PimpedEvent[E] = new PimpedEvent(event)

  /**
    * Request the window focus when the mouse enters the canvas.
    *
    * This ensures that subsequent key events are properly passed to the canvas.
    */
  object RequestFocusOnEnter {
    def mouseEntered(e: MouseEvent): Verdict = {
      if (!e.canvas.hasFocus) {
        e.canvas.requestFocusInWindow()
      }
      Pass
    }

  }

  /**
    * Enables movement in 2D viewports by using the mouse wheel.
    *
    * Essentially, this just maps scroll events to the +/- buttons
    * which are present in a 2D viewport.
    */
  object Scroll2D {

    def mouseWheelMoved(e: MouseWheelEvent): Unit = {
      e.viewport match {
        case _2d: ViewportPanel2D =>
          val button = if (e.getWheelRotation > 0) _2d.positionMinusButton else _2d.positionPlusButton
          button.action.apply()
        case _ =>
      }
    }

  }

  /**
    * Blocks rotation and translation in a 2D viewport.
    *
    * This ensures that a 2D viewport camera remains
    * at the correct angle and focuses the correct point.
    * Zooming is not affected (i.e., allowed).
    */
  object Block2DTranslationAndRotation {
    def mousePressed(e: MouseEvent): Verdict = {
      e.viewport match {
        case _2d: ViewportPanel2D if e.getButton != MouseEvent.BUTTON3 => Block
        case _ => Pass
      }
    }

    def mouseReleased(e: MouseEvent): Verdict = mousePressed(e)

  }

}
