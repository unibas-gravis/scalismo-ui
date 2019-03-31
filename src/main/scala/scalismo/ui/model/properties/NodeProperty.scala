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

import scalismo.ui.event.{ Event, ScalismoPublisher }
import scalismo.ui.model.properties.NodeProperty.event.PropertyChanged

object NodeProperty {

  object event {

    case class PropertyChanged[V](property: NodeProperty[V]) extends Event

  }

}

class NodeProperty[V](initialValue: => V) extends ScalismoPublisher {

  /**
   * Sanitize a value so that it fits into the expected value domain.
   *
   * For instance, the OpacityProperty sanitizes input values to be in [0,1].
   *
   * @param possiblyNotSane a value, possibly not a sane one
   * @return the sanitized version of the value
   */
  protected def sanitize(possiblyNotSane: V) = possiblyNotSane

  private var _value: V = sanitize(initialValue)

  def value: V = _value

  def value_=(newValue: V): Unit = {
    _value = sanitize(newValue)
    publishEvent(PropertyChanged(this))
  }

  override def toString: String = s"${getClass.getName}[$value]"
}
