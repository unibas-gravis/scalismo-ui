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

package scalismo.ui.rendering.actor.mixin

import scalismo.ui.model.properties._
import scalismo.ui.rendering.actor.{ActorEvents, SingleActor}

trait ActorOpacity extends SingleActor with ActorEvents {
  def opacity: OpacityProperty

  listenTo(opacity)

  reactions += {
    case NodeProperty.event.PropertyChanged(p) if p eq opacity => setAppearance()
  }

  private def setAppearance(): Unit = {
    GetProperty().SetOpacity(opacity.value)
    actorChanged()
  }

  setAppearance()

}
