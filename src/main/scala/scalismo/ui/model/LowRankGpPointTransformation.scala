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

