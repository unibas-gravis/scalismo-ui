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

package scalismo.ui.rendering

import java.awt.image.BufferedImage
import java.io.File

import javax.imageio.ImageIO
import scalismo.ui.control.BackgroundColor.event.BackgroundColorChanged
import scalismo.ui.control.{NodeVisibility, SlicingPosition}
import scalismo.ui.model.Scene.event.SceneChanged
import scalismo.ui.model.{Axis, BoundingBox, Renderable}
import scalismo.ui.rendering.RendererPanel.Cameras
import scalismo.ui.rendering.actor._
import scalismo.ui.rendering.actor.mixin.IsImageActor
import scalismo.ui.rendering.internal.RenderingComponent
import scalismo.ui.util.EdtUtil
import scalismo.ui.view.{ViewportPanel, ViewportPanel2D}
import vtk._

import scala.collection.immutable
import scala.swing.{BorderPanel, Component}
import scala.util.Try

object RendererPanel {

  // Helper object to properly set camera positions for 2D slices.
  private[RendererPanel] object Cameras {

    // the state of a freshly created camera.
    case class DefaultCameraState(position: Array[Double], focalPoint: Array[Double], viewUp: Array[Double])

    private var _defaultCameraState: Option[DefaultCameraState] = None

    // this will have an effect exactly once, on the very first invocation.
    def setDefaultCameraState(cam: vtkCamera): Unit = Cameras.synchronized {
      if (_defaultCameraState.isEmpty) {
        _defaultCameraState = Some(DefaultCameraState(cam.GetPosition(), cam.GetFocalPoint(), cam.GetViewUp()))
      }
    }

    def defaultCameraState: DefaultCameraState = _defaultCameraState.get

    case class CameraChangeFromDefault(pitch: Option[Double], roll: Option[Double], yaw: Option[Double])

    def cameraChangeForAxis(axis: Axis): CameraChangeFromDefault = {
      axis match {
        case Axis.Y => CameraChangeFromDefault(Some(-90), None, None)
        case Axis.X => CameraChangeFromDefault(Some(180), Some(-90), Some(270))
        case Axis.Z => CameraChangeFromDefault(Some(180), Some(180), None)
      }
    }
  }

}

class RendererPanel(viewport: ViewportPanel) extends BorderPanel {

  private class RenderableAndActors(val renderable: Renderable, val actorsOption: Option[Actors]) {
    def vtkActors: List[vtkActor] = actorsOption.map(_.vtkActors).getOrElse(Nil)
  }

  private val frame = viewport.frame

  private val implementation = new RenderingComponent(viewport)

  private var attached: Boolean = false

  private var currentActors: List[RenderableAndActors] = Nil

  private var _currentBoundingBox: BoundingBox = BoundingBox.Invalid

  private var updating = false

  def resetCamera(): Unit = {
    implementation.resetCamera()
    render()
  }

  def render(): Unit = {
    implementation.render()
  }

  def screenshot(file: File): Try[Unit] = Try {
    val source = implementation.getComponent
    val image = new BufferedImage(source.getWidth, source.getHeight, BufferedImage.TYPE_INT_RGB)
    val g = image.createGraphics()

    // parameter description: see
    // https://jogamp.org/deployment/jogamp-next/javadoc/jogl/javadoc/javax/media/opengl/awt/GLJPanel.html#setupPrint%28double,%20double,%20int,%20int,%20int%29
    source.setupPrint(1, 1, 0, -1, -1)
    source.printAll(g)
    source.releasePrint()

    image.flush()
    ImageIO.write(image, "png", file)
  }

  def setCameraToAxis(axis: Axis): Unit = {
    val cam = implementation.getActiveCamera
    val default = Cameras.defaultCameraState
    cam.SetPosition(default.position)
    cam.SetFocalPoint(default.focalPoint)
    cam.SetViewUp(default.viewUp)

    val change = Cameras.cameraChangeForAxis(axis)
    change.yaw.foreach(v => cam.Azimuth(v))
    change.pitch.foreach(v => cam.Elevation(v))
    change.roll.foreach(v => cam.Roll(v))
    cam.OrthogonalizeViewUp()
    resetCamera()
  }

  def setAttached(attached: Boolean): Unit = {
    this.attached = attached
    updateAllActors()
  }

  def rendererState: RendererState = implementation.rendererState

  def currentBoundingBox: BoundingBox = _currentBoundingBox

  private def currentBoundingBox_=(newBb: BoundingBox): Unit = {
    if (_currentBoundingBox != newBb) {
      _currentBoundingBox = newBb
      // we take a shortcut here, and directly send on behalf of the viewport.
      viewport.publishEvent(ViewportPanel.event.BoundingBoxChanged(viewport))
    }
  }

  // this is a comparatively expensive operation, so it should only be invoked if something "big" has changed.
  private def updateAllActors(): Unit = {
    // just in case: make sure we're on the correct thread to make VTK happy
    if (!updating) EdtUtil.onEdtWait {
      updating = true
      val renderer = implementation.getRenderer
      renderer.SetBackground(frame.sceneControl.backgroundColor.vtkValue)

      val renderables = if (attached) frame.sceneControl.renderablesFor(viewport) else Nil

      val wasEmptyBefore = currentBoundingBox == BoundingBox.Invalid

      val obsolete = currentActors.filter(ra => !renderables.exists(_ eq ra.renderable))

      val missing = renderables.diff(currentActors.map(_.renderable))
      val created = missing.map(r =>
        new RenderableAndActors(r, ActorsFactory.factoryFor(r).flatMap(f => f.untypedActorsFor(r, viewport)))
      )

      if (obsolete.nonEmpty) {
        obsolete.foreach(ra =>
          ra.vtkActors.foreach { actor =>
            renderer.RemoveActor(actor)
            actor match {
              case dyn: ActorEvents =>
                deafTo(dyn)
                dyn.onDestroy()
              case _ => // do nothing
            }
          }
        )
        currentActors = currentActors diff obsolete
      }

      if (created.nonEmpty) {
        created.foreach(_.vtkActors.foreach { actor =>
          actor match {
            case eventActor: ActorEvents => listenTo(eventActor)
            case _                       =>
          }
          renderer.AddActor(actor)
        })
        currentActors = currentActors ++ created
      }

      if (created.nonEmpty || obsolete.nonEmpty) {

        // Actors have changed, we may have to reorder them.
        // We have to add image actors first, so that all other
        // (non-image) actors are properly drawn on them,
        // instead of the images hiding other actors.

        val originalActors: immutable.IndexedSeq[vtkActor] = {
          val actors = renderer.GetActors()
          val count = actors.GetNumberOfItems()
          if (count > 1) {
            actors.InitTraversal()
            (0 until count) map { _ =>
              actors.GetNextActor()
            }
          } else immutable.IndexedSeq()
        }

        def prioritize(a1: vtkActor, a2: vtkActor): Boolean = {
          (a1, a2) match {
            case (_: IsImageActor, _: IsImageActor) => false
            case (_: IsImageActor, _)               => true
            case (_, _)                             => false
          }
        }

        val reorderedActors = originalActors.sortWith(prioritize)

        if (originalActors != reorderedActors) {
          originalActors.foreach(renderer.RemoveActor)
          reorderedActors.foreach(renderer.AddActor)
        }

        geometryChanged()

        if (wasEmptyBefore) {
          resetCamera()
        } else {
          render()
        }

      }

      updating = false

    }
  }

  private def updateBackgroundOnly(): Unit = EdtUtil.onEdtWait {
    implementation.getRenderer.SetBackground(frame.sceneControl.backgroundColor.vtkValue)
    render()
  }

  private def slicingPositionChanged(pc: SlicingPosition.event.PointChanged): Unit = {
    viewport match {
      case vp2d: ViewportPanel2D =>
        val cam = implementation.getActiveCamera()
        val pos = cam.GetPosition()
        val foc = cam.GetFocalPoint()
        vp2d.axis match {
          case Axis.X =>
            val amount = pc.current.x - pc.previous.x
            pos(0) += amount
            foc(0) += amount
          case Axis.Y =>
            val amount = pc.current.y - pc.previous.y
            pos(1) += amount
            foc(1) += amount
          case Axis.Z =>
            val amount = pc.current.z - pc.previous.z
            pos(2) += amount
            foc(2) += amount
        }
        cam.SetPosition(pos)
        cam.SetFocalPoint(foc)
      case _ => // can't handle
    }
  }

  private def geometryChanged(): Unit = {
    currentBoundingBox = currentActors.foldLeft(BoundingBox.Invalid: BoundingBox)({
      case (bb, actors) =>
        bb.union(actors.actorsOption.map(_.boundingBox).getOrElse(BoundingBox.Invalid))
    })
    render()
  }

  // constructor

  Cameras.setDefaultCameraState(implementation.getActiveCamera)

  layout(Component.wrap(implementation.getComponent)) = BorderPanel.Position.Center

  viewport match {
    case _2d: ViewportPanel2D =>
      listenTo(viewport.frame.sceneControl.slicingPosition)
      setCameraToAxis(_2d.axis)
    case _ => // nothing
  }

  listenTo(frame.scene, frame.sceneControl.nodeVisibility, frame.sceneControl.backgroundColor)

  reactions += {
    case SceneChanged(_) if attached           => updateAllActors()
    case BackgroundColorChanged(_) if attached => updateBackgroundOnly()
    case ActorEvents.event.ActorChanged(_, actorGeometryChanged) if attached =>
      if (actorGeometryChanged) {
        geometryChanged()
      } else {
        render()
      }
    case pc @ SlicingPosition.event.PointChanged(_, _, _)                                         => slicingPositionChanged(pc)
    case NodeVisibility.event.NodeVisibilityChanged(_, view) if attached && view == this.viewport => updateAllActors()
  }

}
