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

  type ValueToColorFunction = Double => Color

  def lowerColor: Color

  def upperColor: Color

  def mappingFunction(scalarRange: ScalarRange): ValueToColorFunction

  def suggestedNumberOfColors: Int

  def description: String

  // needed for the UI to display the description
  override def toString: String = description
}

object ColorMapping {

  case class LinearColorMapping(override val lowerColor: Color, override val upperColor: Color, override val description: String) extends ColorMapping {

    override def mappingFunction(scalarRange: ScalarRange): ValueToColorFunction = {
      value =>
        {

          val lowerLimit = scalarRange.mappedMinimum
          val upperLimit = scalarRange.mappedMaximum

          // edge case: upperLimit=lowerLimit would result in division by 0 in the else branch,
          // so we explicitly defuse that by using <= and >= instead of < and >.
          if (value <= lowerLimit) lowerColor
          else if (value >= upperLimit) upperColor
          else {
            val s = (value - lowerLimit) / (upperLimit - lowerLimit)
            val newColor = (RGB(upperColor) - RGB(lowerColor)) * s + RGB(lowerColor)
            newColor.toAWTColor
          }
        }
    }

    override val suggestedNumberOfColors = 100

  }

  val BlueToRed = LinearColorMapping(Color.BLUE, Color.RED, "Blue-Red")

  val BlackToWhite = LinearColorMapping(Color.BLACK, Color.WHITE, "Black-White")

  val WhiteToBlack = LinearColorMapping(Color.WHITE, Color.BLACK, "White-Black")

  val Default: ColorMapping = BlueToRed
}

