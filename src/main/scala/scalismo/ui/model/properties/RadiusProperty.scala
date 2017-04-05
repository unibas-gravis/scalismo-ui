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

package scalismo.ui.model.properties

object RadiusProperty {
  val DefaultValue: Double = 5.0

}

class RadiusProperty(initialValue: Double) extends NodeProperty[Double](initialValue) {
  def this() = this(RadiusProperty.DefaultValue)

  override protected def sanitize(possiblyNotSane: Double): Double = {
    Math.max(0, possiblyNotSane)
  }
}

trait HasRadius {
  def radius: RadiusProperty
}
