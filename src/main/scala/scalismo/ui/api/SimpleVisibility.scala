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

package scalismo.ui.api

/**
 * Trait to be mixed with ObjectView instances in order to provide visibility changing functionality
 *
 */

sealed trait Viewport {
  val name: String
}

case object xView extends Viewport {
  override val name = "X"
}

case object yView extends Viewport {
  override val name = "Y"
}

case object zView extends Viewport {
  override val name = "Z"
}

case object _3DLeft extends Viewport {
  override val name = "Left"
}

case object _3DRight extends Viewport {
  override val name = "Right"
}

case object _3DMain extends Viewport {
  override val name = "3D"
}

object Viewport {

  val all = Seq(xView, yView, zView, _3DMain, _3DLeft, _3DRight)
  val none = Seq()
  val xOnly: Seq[Viewport] = Seq(xView)
  val yOnly: Seq[Viewport] = Seq(yView)
  val zOnly: Seq[Viewport] = Seq(zView)
  val _2dOnly: Seq[Viewport] = Seq(xView, yView, zView)
  val _3dOnly: Seq[Viewport] = Seq(_3DMain, _3DRight, _3DLeft)
  val _3dLeft: Seq[Viewport] = Seq(_3DLeft)
  val _3dRight: Seq[Viewport] = Seq(_3DRight)
}
