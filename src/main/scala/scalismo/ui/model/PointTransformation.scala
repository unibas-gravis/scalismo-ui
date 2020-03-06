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

package scalismo.ui.model

import scalismo.geometry._3D
import scalismo.registration.{RigidTransformation, RigidTransformationSpace}

/**
 * The general PointTransformation type is simply an alias / another name for
 * "a function that can transform 3D points to other 3D points".
 */
object PointTransformation {
  val RigidIdentity: RigidTransformation[_3D] =
    RigidTransformationSpace[_3D]().transformForParameters(RigidTransformationSpace[_3D]().identityTransformParameters)

  val Identity: PointTransformation = { p =>
    p
  }

}
