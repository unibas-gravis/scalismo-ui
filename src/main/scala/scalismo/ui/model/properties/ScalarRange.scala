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

/**
 * Scalar range - used for the visualization of ranges of scalar values.
 *
 * @param domainMinimum minimum value present in the data.
 * @param domainMaximum maximum value present in the data.
 * @param mappedMinimum minimum value for color mapping. This, and values below, will be mapped to the lower color.
 * @param mappedMaximum maximum value for color mapping. This, and values above, will be mapped to the upper color.
 * @param colorMapping  color mapping.
 */
case class ScalarRange(domainMinimum: Float, domainMaximum: Float, mappedMinimum: Float, mappedMaximum: Float, colorMapping: ColorMapping) {
}

object ScalarRange {
  // convenience constructor
  def apply(domainMinimum: Float, domainMaximum: Float, colorMapping: ColorMapping = ColorMapping.Default): ScalarRange = {
    ScalarRange(domainMinimum, domainMaximum, domainMinimum, domainMaximum, colorMapping)
  }
}
