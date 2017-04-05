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

package scalismo.ui.rendering.actor

import scalismo.ui.event.{ Event, ScalismoPublisher }

import scala.collection.mutable.ArrayBuffer
import scala.swing.Publisher

object ActorEvents {

  object event {

    case class ActorChanged(source: ActorEvents, didGeometryChange: Boolean) extends Event

  }

}

trait ActorEvents extends ScalismoPublisher {

  private lazy val listening: ArrayBuffer[Publisher] = new ArrayBuffer

  override def listenTo(ps: Publisher*): Unit = {
    listening ++= ps
    super.listenTo(ps: _*)
  }

  override def deafTo(ps: Publisher*): Unit = {
    listening --= ps
    super.deafTo(ps: _*)
  }

  //  def actorPropertyChanged(): Unit = {
  //    publishEvent(ActorEvents.event.ActorPropertyChanged(this))
  //  }
  //
  //  def actorGeometryChanged(): Unit = {
  //    publishEvent(ActorEvents.event.ActorGeometryChanged(this))
  //  }
  def actorChanged(didGeometryChange: Boolean = false) = {
    publishEvent(ActorEvents.event.ActorChanged(this, didGeometryChange))
  }

  /*
   * Invoked when this actor is destroyed. If you override this
   * method, you MUST invoke super.onDestroy() in your implementation.
   */
  def onDestroy(): Unit = {
    // we create an immutable copy of listening first, to prevent weird results
    deafTo(listening.toList: _*)
  }
}