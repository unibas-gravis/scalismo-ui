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

object ScalarRangeProperty {
  val DefaultValue: ScalarRange = ScalarRange(0, 1, 0, 1)
  val DefaultColorMap: ColorMapping = BlueToRedColorMapping
}

class ScalarRangeProperty(initialValue: ScalarRange, var colorMapping: ColorMapping = ScalarRangeProperty.DefaultColorMap) extends NodeProperty[ScalarRange](initialValue) {
  def this() = this(
    ScalarRangeProperty.DefaultValue,
    ScalarRangeProperty.DefaultColorMap
  )

  val scalarRange: ScalarRange = sanitize(initialValue)

  override protected def sanitize(newValue: ScalarRange): ScalarRange = {
    if (newValue.absoluteMaximum < newValue.absoluteMinimum) {
      sanitize(newValue.copy(absoluteMaximum = newValue.absoluteMinimum))
    } else if (newValue.cappedMaximum > newValue.absoluteMaximum) {
      sanitize(newValue.copy(cappedMaximum = newValue.absoluteMaximum))
    } else if (newValue.cappedMinimum < newValue.absoluteMinimum) {
      sanitize(newValue.copy(cappedMinimum = newValue.absoluteMinimum))
    } else if (newValue.cappedMaximum < newValue.cappedMinimum) {
      sanitize(newValue.copy(cappedMinimum = newValue.cappedMaximum))
    } else newValue
  }
}

trait HasScalarRange {
  def scalarRange: ScalarRangeProperty
}

