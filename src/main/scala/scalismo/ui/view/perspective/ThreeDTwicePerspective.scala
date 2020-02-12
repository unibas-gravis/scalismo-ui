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

import scalismo.ui.view.{ScalismoFrame, ViewportPanel, ViewportPanel3D}

import scala.swing.GridPanel

class ThreeDTwicePerspective(override val frame: ScalismoFrame, override val factory: PerspectiveFactory)
    extends GridPanel(1, 2)
    with Perspective {
  override val viewports: List[ViewportPanel] =
    List(new ViewportPanel3D(frame, "Left"), new ViewportPanel3D(frame, "Right"))

  contents ++= viewports
}

object ThreeDTwicePerspective extends PerspectiveFactory {
  override def instantiate(frame: ScalismoFrame): Perspective = new ThreeDTwicePerspective(frame, this)

  override val perspectiveName: String = "Two 3D viewports"
}
