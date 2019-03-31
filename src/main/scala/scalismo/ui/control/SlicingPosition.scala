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

package scalismo.ui.control

import scalismo.geometry.{ Point, Point3D }
import scalismo.ui.control.SlicingPosition.event
import scalismo.ui.control.SlicingPosition.renderable.BoundingBoxRenderable
import scalismo.ui.event.{ Event, ScalismoPublisher }
import scalismo.ui.model.{ BoundingBox, Renderable, Scene }
import scalismo.ui.settings.GlobalSettings
import scalismo.ui.view._

object SlicingPosition {

  object event {

    case class VisibilityChanged(source: SlicingPosition) extends Event

    case class PointChanged(source: SlicingPosition, previous: Point3D, current: Point3D) extends Event

    case class BoundingBoxChanged(source: SlicingPosition) extends Event

    case class PerspectiveChanged(source: SlicingPosition) extends Event

  }

  object renderable {

    class BoundingBoxRenderable(val source: SlicingPosition) extends Renderable

  }

}

class SlicingPosition(val scene: Scene, val frame: ScalismoFrame) extends ScalismoPublisher {

  private var _visible = GlobalSettings.get[Boolean](GlobalSettings.Keys.SlicingPositionShow).getOrElse(false)

  def visible = _visible

  def visible_=(newVisible: Boolean): Unit = {
    if (_visible != newVisible) {
      _visible = newVisible
      GlobalSettings.set(GlobalSettings.Keys.SlicingPositionShow, newVisible)
      publishEvent(event.VisibilityChanged(this))
      //scene.publishEdt(Scene.SlicingPosition.SlicesVisibleChanged(this))
      //scene.publishEdt(Scene.VisibilityChanged(scene))
    }
  }

  private var _point: Point3D = Point3D(0, 0, 0)

  def point = {
    _point
  }

  def point_=(np: Point3D): Unit = {
    if (_point != np) {
      val prev = _point
      _point = np
      publishEvent(event.PointChanged(this, prev, np))
    }
  }

  def x = point(0)

  def y = point(1)

  def z = point(2)

  def x_=(nv: Float): Unit = {
    val sv = Math.min(Math.max(boundingBox.xMin, nv), boundingBox.xMax)
    if (x != sv) {
      point_=(Point(sv, y, z))
    }
  }

  def y_=(nv: Float): Unit = {
    val sv = Math.min(Math.max(boundingBox.yMin, nv), boundingBox.yMax)
    if (y != sv) {
      point = Point(x, sv, z)
    }
  }

  def z_=(nv: Float): Unit = {
    val sv = Math.min(Math.max(boundingBox.zMin, nv), boundingBox.zMax)
    if (z != sv) {
      point = Point(x, y, sv)
    }
  }

  private def sanitizePoint(): Unit = {
    val sx = Math.min(Math.max(boundingBox.xMin, x), boundingBox.xMax)
    val sy = Math.min(Math.max(boundingBox.yMin, y), boundingBox.yMax)
    val sz = Math.min(Math.max(boundingBox.zMin, z), boundingBox.zMax)
    point = Point(sx, sy, sz)
  }

  private var _boundingBox: BoundingBox = BoundingBox.Invalid

  def boundingBox = _boundingBox

  private def boundingBox_=(nb: BoundingBox): Unit = {
    if (_boundingBox != nb) {
      val wasInvalid = _boundingBox == BoundingBox.Invalid
      _boundingBox = nb

      publishEvent(event.BoundingBoxChanged(this))
      if (wasInvalid) center()
      sanitizePoint()
    }
  }

  def viewports: List[ViewportPanel] = frame.perspective.viewports

  private def updateBoundingBox(): Unit = {
    boundingBox = viewports.foldLeft(BoundingBox.Invalid: BoundingBox)({
      case (bb, vp) =>
        bb.union(vp.currentBoundingBox)
    })
  }

  private def perspectiveChanged(): Unit = {
    viewports.foreach(vp => listenTo(vp))
    updateBoundingBox()
    publishEvent(event.PerspectiveChanged(this))
  }

  def center(): Unit = {
    point = boundingBox.center
  }

  reactions += {
    case PerspectivePanel.event.PerspectiveChanged(_, _, _) => perspectiveChanged()
    case ViewportPanel.event.BoundingBoxChanged(_) => updateBoundingBox()
    case ViewportPanel.event.Detached(vp) => deafTo(vp)
  }

  def initialize(): Unit = {
    listenTo(frame.perspective)
    perspectiveChanged()
  }

  // renderables
  private lazy val boundingBoxRenderable = new BoundingBoxRenderable(this)

  def renderablesFor(viewport: ViewportPanel): List[Renderable] = {
    viewport match {
      case _: ViewportPanel3D => List(boundingBoxRenderable)
      case _: ViewportPanel2D => List(boundingBoxRenderable)
    }
  }
}
