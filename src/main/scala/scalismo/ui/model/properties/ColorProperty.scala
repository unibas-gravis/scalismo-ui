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

import scalismo.ui.event.{ Event, ScalismoPublisher }

object ColorProperty extends ScalismoPublisher {
  val DefaultValue: Color = Color.WHITE

  object event {

    /* this is a simple "shortcut" event that gets published whenever *any* color property is changed.
    * It's used for keeping the Nodes View updated, without that view having to listen to all colorable objects
    */
    case object SomeColorPropertyChanged extends Event

  }

}

class ColorProperty(initialValue: Color) extends NodeProperty[Color](initialValue) {
  def this() = this(ColorProperty.DefaultValue)

  override def value_=(newValue: Color) = {
    super.value_=(newValue)
    ColorProperty.publishEvent(ColorProperty.event.SomeColorPropertyChanged)
  }
}

trait HasColor {
  def color: ColorProperty
}
