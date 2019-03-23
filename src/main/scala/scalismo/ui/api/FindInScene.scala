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

import scalismo.ui.model.SceneNode

/**
 * This typeclass needs to be implemented for a type V (a view) if the user should be
 * able to search for a view
 */
protected[api] trait FindInScene[V] {
  def createView(s: SceneNode): Option[V]

}

object FindInScene {

  def apply[A](implicit a: FindInScene[A]): FindInScene[A] = a

}
