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

import javax.swing.JComponent
import scalismo.ui.event.ScalismoPublisher

import scala.swing.{Component, Slider}
import scala.swing.event.ValueChanged

class SubDividedSliderAdapter(val slider: Slider, val factor: Double = 100.0) extends Component with ScalismoPublisher {

  override lazy val peer: JComponent = slider.peer

  def up(): Unit = {
    if (slider.value < slider.max) {
      slider.value = slider.value + 1
    }
  }

  def down(): Unit = {
    if (slider.value > slider.min) {
      slider.value = slider.value - 1
    }
  }

  def min: Double = slider.min / factor

  def min_=(v: Double) {
    slider.min = Math.floor(v * factor).toInt
  }

  def max: Double = slider.max / factor

  def max_=(v: Double) {
    slider.max = Math.ceil(v * factor).toInt
  }

  def value: Double = slider.value / factor

  def value_=(v: Double) {
    slider.value = Math.round(v * factor).toInt
  }

  def listenToOwnEvents() = listenTo(slider)

  def deafToOwnEvents() = deafTo(slider)

  reactions += {
    case ValueChanged(s: Slider) if s == slider => publishEvent(new ValueChanged(SubDividedSliderAdapter.this))
  }

  listenToOwnEvents()
}
