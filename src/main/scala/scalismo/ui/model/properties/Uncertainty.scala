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

import scalismo.geometry.EuclideanVector._
import scalismo.geometry._
import scalismo.statisticalmodel.MultivariateNormalDistribution

object Uncertainty {
  val DefaultAxes = List(EuclideanVector3D(1, 0, 0), EuclideanVector3D(0, 1, 0), EuclideanVector3D(0, 0, 1))
  var DefaultSigmas: List[Double] = List(1, 1, 1)
  var DefaultUncertainty: Uncertainty = Uncertainty(DefaultAxes, DefaultSigmas)

  def apply(distribution: MultivariateNormalDistribution): Uncertainty = {
    val (axes, sigmas) = distribution.principalComponents.toList.map { case (a, v) => (parametricToConcrete3D(EuclideanVector[_3D](a.toArray)), Math.sqrt(v)) }.unzip
    Uncertainty(axes, sigmas)
  }
}

case class Uncertainty(axes: List[EuclideanVector3D], sigmas: List[Double]) {
  require(axes.length == 3 && sigmas.length == 3)

  // FIXME: require that axes are perpendicular and have a norm of 1

  def toMultivariateNormalDistribution: MultivariateNormalDistribution = {
    val variances = sigmas.map(f => f * f)
    val mean = EuclideanVector3D(0, 0, 0)
    MultivariateNormalDistribution(mean.toBreezeVector, axes.map(_.toBreezeVector).zip(variances))

  }

  def rotationMatrix: SquareMatrix[_3D] = {
    val candidate = SquareMatrix[_3D](axes.flatMap(_.toArray).toArray)
    if (breeze.linalg.det(candidate.toBreezeMatrix) < 0) {
      // improper rotation matrix
      SquareMatrix(candidate.data.map { f => -f })
    } else candidate
  }
}
