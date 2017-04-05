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

import breeze.linalg.DenseVector
import scalismo.geometry.{ Point, _3D, Vector }
import scalismo.statisticalmodel.{ DiscreteLowRankGaussianProcess, LowRankGaussianProcess }

// This used to be a case class, but since it is extended by the discrete version, it can no longer be.
// Therefore, the copy methods have to be defined manually.
class LowRankGpPointTransformation protected (val gp: LowRankGaussianProcess[_3D, Vector[_3D]], val coefficients: DenseVector[Double]) extends PointTransformation {

  lazy val vectorField = gp.instance(coefficients)

  override def apply(point: Point[_3D]): Point[_3D] = {
    point + vectorField(point)
  }

  def copy(coefficients: DenseVector[Double]): LowRankGpPointTransformation = new LowRankGpPointTransformation(gp, coefficients)
}

object LowRankGpPointTransformation {
  def apply(gp: LowRankGaussianProcess[_3D, Vector[_3D]], coefficients: DenseVector[Double]): LowRankGpPointTransformation = new LowRankGpPointTransformation(gp, coefficients)

  def apply(gp: LowRankGaussianProcess[_3D, Vector[_3D]]): LowRankGpPointTransformation = apply(gp, DenseVector.zeros[Double](gp.rank))
}

class DiscreteLowRankGpPointTransformation private (val dgp: DiscreteLowRankGaussianProcess[_3D, Vector[_3D]], gp: LowRankGaussianProcess[_3D, Vector[_3D]], coefficients: DenseVector[Double]) extends LowRankGpPointTransformation(gp, coefficients) {

  protected def this(dgp: DiscreteLowRankGaussianProcess[_3D, Vector[_3D]], coefficients: DenseVector[Double]) = this(dgp, dgp.interpolateNearestNeighbor, coefficients)

  // no need to re-interpolate if the gp didn't change
  override def copy(coefficients: DenseVector[Double]): DiscreteLowRankGpPointTransformation = new DiscreteLowRankGpPointTransformation(dgp, gp, coefficients)
}

object DiscreteLowRankGpPointTransformation {
  def apply(dgp: DiscreteLowRankGaussianProcess[_3D, Vector[_3D]]): DiscreteLowRankGpPointTransformation = apply(dgp, DenseVector.zeros[Double](dgp.rank))

  def apply(dgp: DiscreteLowRankGaussianProcess[_3D, Vector[_3D]], coefficients: DenseVector[Double]): DiscreteLowRankGpPointTransformation = new DiscreteLowRankGpPointTransformation(dgp, coefficients)
}

