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
import scalismo.ui.view.{ScalismoFrame, ViewportPanel, ViewportPanel2D, ViewportPanel3D}

import scala.swing.GridPanel

class OrthogonalPerspective(override val frame: ScalismoFrame, override val factory: PerspectiveFactory)
    extends GridPanel(2, 2)
    with Perspective {
  override val viewports: List[ViewportPanel] = List(new ViewportPanel3D(frame),
                                                     new ViewportPanel2D(frame, Axis.X),
                                                     new ViewportPanel2D(frame, Axis.Y),
                                                     new ViewportPanel2D(frame, Axis.Z))
  contents ++= viewports
}

object OrthogonalPerspective extends PerspectiveFactory {
  override def instantiate(frame: ScalismoFrame): Perspective = new OrthogonalPerspective(frame, this)

  override def perspectiveName: String = "Orthogonal Slices"
}
