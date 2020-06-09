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

import scala.swing.Slider

class SubDividedSliderAdapter(private val _slider: Slider, private val factor: Double = 100.0) {

  def slider: Slider = _slider

  def up(): Unit = {
    if (_slider.value < _slider.max) {
      _slider.value = _slider.value + 1
    }
  }

  def down(): Unit = {
    if (_slider.value > _slider.min) {
      _slider.value = _slider.value - 1
    }
  }

  def min: Double = _slider.min / factor
  def min_=(v: Double) { _slider.min = Math.floor(v * factor).toInt }
  def max: Double = _slider.max / factor
  def max_=(v: Double) { _slider.max = Math.ceil(v * factor).toInt }
  def value: Double = _slider.value / factor
  def value_=(v: Double) { _slider.value = Math.round(v * factor).toInt }
}
