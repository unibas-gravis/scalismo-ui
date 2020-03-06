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

package scalismo.ui.rendering.util

import java.awt.Color

import scalismo.ui.model.BoundingBox

object VtkUtil {

  def bounds2BoundingBox(bounds: Array[Double]): BoundingBox = {
    val f = bounds.map(f => f.toFloat)
    BoundingBox(f(0), f(1), f(2), f(3), f(4), f(5))
  }

  def colorToArray(color: Color): Array[Double] = {
    color.getRGBColorComponents(null).map(_.toDouble)
  }
}
