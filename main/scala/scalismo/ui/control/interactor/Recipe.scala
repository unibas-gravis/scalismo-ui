package scalismo.ui.control.interactor

import java.awt.Point
import java.awt.event.{ InputEvent, KeyEvent, MouseEvent, MouseWheelEvent }

import scalismo.geometry.{ Landmark, _3D }
import scalismo.ui.control.interactor.Interactor.Verdict.{ Block, Pass }
import scalismo.ui.control.interactor.Interactor.{ PimpedEvent, Verdict }
import scalismo.ui.model.capabilities.{ Grouped, InverseTransformation }
import scalismo.ui.model.properties.Uncertainty
import scalismo.ui.model.{ LandmarkNode, ImageNode, SceneNode, StatusMessage }
import scalismo.ui.rendering.RendererState.PointAndNode
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
 * and the methods they provide generally mimick the methods
 * found in the Interactor trait.
 */
object Recipe {
  implicit def pimpEvent[E <: InputEvent](event: E): PimpedEvent[E] = new PimpedEvent(event)

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

    def mouseMoved(e: MouseEvent, approve: SceneNode => Boolean): Verdict = {
      e.viewport match {
        case _2d: ViewportPanel2D =>
          val state = _2d.rendererState

          val newHighlighted = state.pointAndNodeAtPosition(e.getPoint).nodeOption match {
            case Some(node) if state.isHighlightable(node) && approve(node) => Some(node)
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
          case skip: LandmarkNode => None
          case ok: Grouped with InverseTransformation =>
            val name = ok.group.landmarks.nameGenerator.nextName()
            val point = ok.inverseTransform(pointAndNode.pointOption.get)
            ok.group.landmarks.add(point, name, Uncertainty.DefaultUncertainty)
          case ok: ImageNode =>
            val name = ok.group.landmarks.nameGenerator.nextName()
            // images don't support transformations
            val lm = new Landmark[_3D](name, pointAndNode.pointOption.get)
            ok.group.landmarks.add(lm)
          case _ =>
        }
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

    def mouseWheelMoved(e: MouseWheelEvent): Verdict = {
      e.viewport match {
        case _2d: ViewportPanel2D =>
          val button = if (e.getWheelRotation > 0) _2d.positionMinusButton else _2d.positionPlusButton
          button.action.apply()
        case _ =>
      }
      Pass
    }

  }

  /**
   * Blocks rotation in a 2D viewport.
   *
   * This ensures that a 2D viewport camera remains
   * at the correct angle and focuses the correct point.
   * Zooming and translation are not affected (i.e., allowed).
   */
  object Block2DRotation {
    def mousePressed(e: MouseEvent): Verdict = {
      e.viewport match {
        case _2d: ViewportPanel2D if e.getButton == MouseEvent.BUTTON1 => Block
        case _ => Pass
      }
    }

    def mouseReleased(e: MouseEvent): Verdict = mousePressed(e)

  }

  object ShiftKeySetsSlicePosition {
    private var active: Boolean = false
    private var point = new Point

    def keyPressedOrReleased(e: KeyEvent): Verdict = {
      // 1 is the shift key
      active = (e.getModifiers & 1) == 1
      updateSlicePosition(e)
    }

    def mouseMoved(e: MouseEvent): Verdict = {
      point = e.getPoint
      updateSlicePosition(e)
    }

    private def updateSlicePosition(e: InputEvent): Verdict = {
      if (active) {
        e.viewport.rendererState.pointAndNodeAtPosition(point).pointOption match {
          case Some(position) => e.viewport.frame.sceneControl.slicingPosition.point = position
          case _ =>
        }
      }
      Pass
    }
  }

  object ControlKeyShowsImageInformation {
    private var active: Boolean = false
    private var point = new Point

    def keyPressedOrReleased(e: KeyEvent): Verdict = {
      // 2 is the control key
      active = (e.getModifiers & 2) == 2
      showInformation(e)
    }

    def mouseMoved(e: MouseEvent): Verdict = {
      point = e.getPoint
      showInformation(e)
    }

    private def showInformation(e: InputEvent): Verdict = {
      if (active) {
        val pointAndNode = e.viewport.rendererState.pointAndNodeAtPosition(point)
        pointAndNode match {
          case PointAndNode(Some(p3d), Some(img: ImageNode)) =>
            val ptId = img.source.domain.findClosestPoint(p3d)
            val pt = ptId.point
            val intensity = img.source(ptId.id)

            val message = StatusMessage(f"${img.name}(${pt.x}%2.2f,${pt.y}%2.2f,${pt.z}%2.2f) = $intensity%2.2f", log = false)
            e.viewport.frame.status.set(message)

          case _ =>
        }

      }
      Pass
    }
  }

}
