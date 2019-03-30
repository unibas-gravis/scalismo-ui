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

package scalismo.ui.api

import scalismo.common.UnstructuredPointsDomain3D
import scalismo.geometry.{ Point, _3D }

/**
 * A dummy class used only in the api to be able to show the transformation glyphs using a uniform syntax
 */
case class TransformationGlyph(points: IndexedSeq[Point[_3D]])

object TransformationGlyph {
  def apply(domain: UnstructuredPointsDomain3D): TransformationGlyph = new TransformationGlyph(domain.pointSequence)
}
