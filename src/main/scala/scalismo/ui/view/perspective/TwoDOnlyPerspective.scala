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

package scalismo.ui.view.perspective

import scalismo.ui.model.Axis
import scalismo.ui.view.{ScalismoFrame, ViewportPanel, ViewportPanel2D}

import scala.swing.BorderPanel

object TwoDOnlyPerspective {

  object X extends PerspectiveFactory {
    override def instantiate(frame: ScalismoFrame): Perspective = new TwoDOnlyPerspective(frame, Axis.X, this)

    override val perspectiveName: String = "X Slice"
  }

  object Y extends PerspectiveFactory {
    override def instantiate(frame: ScalismoFrame): Perspective = new TwoDOnlyPerspective(frame, Axis.Y, this)

    override val perspectiveName: String = "Y Slice"
  }

  object Z extends PerspectiveFactory {
    override def instantiate(frame: ScalismoFrame): Perspective = new TwoDOnlyPerspective(frame, Axis.Z, this)

    override val perspectiveName: String = "Z Slice"
  }

}

class TwoDOnlyPerspective(override val frame: ScalismoFrame, axis: Axis, override val factory: PerspectiveFactory)
    extends BorderPanel
    with Perspective {
  val viewport = new ViewportPanel2D(frame, axis)

  override val viewports: List[ViewportPanel] = List(viewport)

  layout(viewport) = BorderPanel.Position.Center
}
