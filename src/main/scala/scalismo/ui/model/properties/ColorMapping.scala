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

import java.awt.Color

import scalismo.color.RGB

/** Maps a range of values, determined by a lower and upper value to a color */
trait ColorMapping {

  var lowerColor: Color

  var upperColor: Color

  def mappingFunction(scalarRange: ScalarRange): (Double => Color)

  def suggestedNumberOfColors: Int
}

private[properties] case class LinearColorMapping(lColor: Color, uColor: Color) extends ColorMapping {

  override var lowerColor = lColor
  override var upperColor = uColor

  override def mappingFunction(scalarRange: ScalarRange): (Double => Color) = {
    value =>
      {

        val lowerValue = scalarRange.cappedMinimum
        val upperValue = scalarRange.cappedMaximum
        if (value < lowerValue) lColor
        else if (value > upperValue) uColor
        else {
          val s = (value - lowerValue) / (upperValue - lowerValue)
          val newColor = (RGB(uColor) - RGB(lColor)) * s + RGB(lColor)
          newColor.toAWTColor
        }
      }
  }

  override val suggestedNumberOfColors = 100

}

object BlueToRedColorMapping extends LinearColorMapping(Color.BLUE, Color.RED)

object WhiteToBlackMapping extends LinearColorMapping(Color.WHITE, Color.BLACK)
