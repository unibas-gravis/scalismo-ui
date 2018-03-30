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

package scalismo.ui.control

import java.awt.Color

import scalismo.ui.control.BackgroundColor.event.BackgroundColorChanged
import scalismo.ui.event.{ Event, ScalismoPublisher }
import scalismo.ui.rendering.util.VtkUtil

object BackgroundColor {

  object event {

    case class BackgroundColorChanged(source: BackgroundColor) extends Event

  }

}

class BackgroundColor extends ScalismoPublisher {

  def value: Color = _value

  def value_=(newColor: Color): Unit = {
    _value = newColor
    _vtkValue = VtkUtil.colorToArray(_value)
    publishEvent(BackgroundColorChanged(this))
  }

  private var _value: Color = Color.BLACK
  private var _vtkValue = VtkUtil.colorToArray(_value)

  def vtkValue: Array[Double] = _vtkValue
}
