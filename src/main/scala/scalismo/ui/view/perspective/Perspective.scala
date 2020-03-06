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

import scalismo.ui.view.util.CardPanel
import scalismo.ui.view.{ScalismoFrame, ViewportPanel}

trait Perspective extends CardPanel.ComponentWithUniqueId {
  def factory: PerspectiveFactory

  def frame: ScalismoFrame

  def viewports: List[ViewportPanel]

  final override val uniqueId = factory.perspectiveName

  override def toString: String = factory.perspectiveName
}

object Perspective {}
