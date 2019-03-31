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

package scalismo.ui.view.util

class FloatSlider(val minFloat: Float, val maxFloat: Float, val stepFloat: Float) extends FancySlider {

  private def f2i(f: Float): Int = {
    val sf = Math.max(minFloat, Math.min(maxFloat, f))
    Math.round((sf - minFloat) / stepFloat)
  }

  private def i2f(i: Int): Float = {
    minFloat + stepFloat * i
  }

  def floatValue = i2f(value)

  def floatValue_=(newValue: Float): Unit = value = f2i(newValue)

  min = 0
  max = ((maxFloat - minFloat) / stepFloat).toInt
  value = min + (max - min) / 2

  // intended to be overwritten in subclasses if needed
  override def formattedValue(sliderValue: Int): String = {
    f"${i2f(sliderValue)}%1.1f"
  }
}

