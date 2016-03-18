package scalismo.ui.control.interactor

import java.awt.AWTEvent
import java.awt.event.{ MouseEvent, MouseWheelEvent }

import scalismo.geometry.{ _3D, Landmark }
import scalismo.ui.control.interactor.Interactor.Verdict.{ Pass, Block }
import scalismo.ui.control.interactor.Interactor.{ Verdict, PimpedEvent }
import scalismo.ui.model.{ ImageNode, SceneNode }
import scalismo.ui.model.capabilities.{ InverseTransformation, Grouped }
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
   * In 2D viewports, highlight the outline of the object that would be picked
   * when a pick request comes in.
   */
  object HighlightOutlineOfPickableObject {
    var highlighted: Option[SceneNode] = None

    def mouseMoved(e: MouseEvent): Verdict = {
      e.viewport match {
        case _2d: ViewportPanel2D =>
          val state = _2d.rendererState

          val newHighlighted = state.pointAndNodeAtPosition(e.getPoint).nodeOption match {
            case Some(node) if state.isHighlightable(node) => Some(node)
            case _ => None
          }

          if (newHighlighted != highlighted) {
            highlighted.foreach { h =>
              state.setHighlighted(h, onOff = false)
            }
            newHighlighted.foreach { h =>
              state.setHighlighted(h, onOff = true)
            }
            highlighted = newHighlighted
          }

        case _ => Pass
      }
      Pass
    }
  }

  object AddLandmarkOnClick {
    def mouseClicked(e: MouseEvent): Verdict = {
      val pointAndNode = e.viewport.rendererState.pointAndNodeAtPosition(e.getPoint)
      pointAndNode.nodeOption.foreach { node =>
        node match {
          case ok: Grouped with InverseTransformation =>
            val name = ok.group.landmarks.nameGenerator.nextName()
            val lm = new Landmark[_3D](name, ok.inverseTransform(pointAndNode.pointOption.get))
            ok.group.landmarks.add(lm)
          case ok: ImageNode =>
            val name = ok.group.landmarks.nameGenerator.nextName()
            // images don't support transformations
            val lm = new Landmark[_3D](name, pointAndNode.pointOption.get)
            ok.group.landmarks.add(lm)
        }
      }
      Block
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
