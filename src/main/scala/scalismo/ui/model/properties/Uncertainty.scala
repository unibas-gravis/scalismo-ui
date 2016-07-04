package scalismo.ui.model.properties

import scalismo.geometry.Vector._
import scalismo.geometry._
import scalismo.statisticalmodel.{ MultivariateNormalDistribution, NDimensionalNormalDistribution }

object Uncertainty {
  val DefaultAxes = List(Vector3D(1, 0, 0), Vector3D(0, 1, 0), Vector3D(0, 0, 1))
  var DefaultSigmas: List[Double] = List(1, 1, 1)
  var DefaultUncertainty: Uncertainty = Uncertainty(DefaultAxes, DefaultSigmas)

  def apply(distribution: MultivariateNormalDistribution): Uncertainty = {
    val (axes, sigmas) = distribution.principalComponents.toList.map { case (a, v) => (parametricToConcrete3D(Vector[_3D](a.toArray)), Math.sqrt(v)) }.unzip
    Uncertainty(axes, sigmas)
  }
}

case class Uncertainty(axes: List[Vector3D], sigmas: List[Double]) {
  require(axes.length == 3 && sigmas.length == 3)

  // FIXME: require that axes are perpendicular and have a norm of 1

  def toMultivariateNormalDistribution: MultivariateNormalDistribution = {
    val variances = sigmas.map(f => f * f)
    val mean = Vector3D(0, 0, 0)
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
