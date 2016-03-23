package scalismo.ui.model.properties

import scalismo.geometry.{ SquareMatrix, Vector, _3D, Vector3D }
import scalismo.statisticalmodel.NDimensionalNormalDistribution

object Uncertainty {
  var DefaultSigmas: List[Float] = List(30, 20, 10)
  var DefaultUncertainty: Uncertainty = Uncertainty(List(Vector3D(1, 0, 0), Vector3D(0, 1, 0), Vector3D(0, 0, 1)), DefaultSigmas)

  def apply(distribution: NDimensionalNormalDistribution[_3D]): Uncertainty = {
    val (axes, sigmas) = distribution.principalComponents.toList.map { case (a, v) => (a: Vector3D, Math.sqrt(v).toFloat) }.unzip
    Uncertainty(axes, sigmas)
  }
}

case class Uncertainty(axes: List[Vector3D], sigmas: List[Float]) {
  require(axes.length == 3 && sigmas.length == 3)
  // FIXME: require that axes are perpendicular and have a norm of 1

  def to3DNormalDistribution: NDimensionalNormalDistribution[_3D] = {
    val variances = sigmas.map(f => f * f)
    val mean = Vector3D(0, 0, 0)
    NDimensionalNormalDistribution(mean, axes.zip(variances))
  }

  def rotationMatrix: SquareMatrix[_3D] = {
    val candidate = SquareMatrix[_3D](axes.flatMap(_.toArray).toArray)
    if (breeze.linalg.det(candidate.toBreezeMatrix) < 0) {
      // improper rotation matrix
      SquareMatrix(candidate.data.map { f => -f })
    } else candidate
  }
}
