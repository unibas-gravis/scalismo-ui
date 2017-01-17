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

package scalismo.ui.view.perspective

import scalismo.ui.settings.GlobalSettings
import scalismo.ui.view.ScalismoFrame

/**
 * Global singleton containing data related to available perspectives.
 * To allow for customization, available perspectives can be added (or removed)
 * by calling the addFactory and removeFactory methods. If needed, the defaultPerspective
 * can also be overwritten (that's why it's a var).
 *
 * It is advisable to do any necessary modifications right at the beginning of the program,
 * before a frame is actually created.
 */
object PerspectiveFactory {
  final val BuiltinFactories: List[PerspectiveFactory] = List(
    OrthogonalPerspective,
    ThreeDOnlyPerspective,
    ThreeDTwicePerspective,
    TwoDOnlyPerspective.X,
    TwoDOnlyPerspective.Y,
    TwoDOnlyPerspective.Z
  )

  private var _factories: List[PerspectiveFactory] = BuiltinFactories

  def factories: List[PerspectiveFactory] = _factories

  def addFactory(factory: PerspectiveFactory) = {
    _factories ++= List(factory)
  }

  def removeFactory(factory: PerspectiveFactory) = {
    _factories = factories.filterNot(_ == factory)
  }

  var defaultPerspective: PerspectiveFactory = {
    val userPreferred = GlobalSettings.get[String](GlobalSettings.Keys.PerspectiveName).flatMap { name => factories.find(_.perspectiveName == name) }
    userPreferred.getOrElse(factories.head)
  }

}

trait PerspectiveFactory {
  /**
   * Name of the perspective. This *MUST* be globally unique.
   *
   * @return the unique, human-readable name of the perspective.
   */
  def perspectiveName: String

  def instantiate(frame: ScalismoFrame): Perspective
}
