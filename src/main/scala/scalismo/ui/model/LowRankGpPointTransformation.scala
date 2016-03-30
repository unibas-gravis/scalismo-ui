package scalismo.ui.model

import breeze.linalg.DenseVector
import scalismo.geometry.{Point, _3D}
import scalismo.statisticalmodel.{DiscreteLowRankGaussianProcess, LowRankGaussianProcess}

// This used to be a case class, but since it is extended by the discrete version, it can no longer be.
// Therefore, the copy methods have to be defined manually.
class LowRankGpPointTransformation protected (val gp: LowRankGaussianProcess[_3D, _3D], val coefficients: DenseVector[Float]) extends PointTransformation {

  lazy val vectorField = gp.instance(coefficients)

  override def apply(point: Point[_3D]): Point[_3D] = {
    point + vectorField(point)
  }

  def copy(coefficients: DenseVector[Float]): LowRankGpPointTransformation = new LowRankGpPointTransformation(gp, coefficients)
}

object LowRankGpPointTransformation {
  def apply(gp: LowRankGaussianProcess[_3D, _3D], coefficients: DenseVector[Float]): LowRankGpPointTransformation = new LowRankGpPointTransformation(gp, coefficients)

  def apply(gp: LowRankGaussianProcess[_3D, _3D]): LowRankGpPointTransformation = apply(gp, DenseVector.zeros[Float](gp.rank))
}

class DiscreteLowRankGpPointTransformation private (val dgp: DiscreteLowRankGaussianProcess[_3D, _3D], gp: LowRankGaussianProcess[_3D, _3D], coefficients: DenseVector[Float]) extends LowRankGpPointTransformation(gp, coefficients) {

  protected def this(dgp: DiscreteLowRankGaussianProcess[_3D, _3D], coefficients: DenseVector[Float]) = this(dgp, dgp.interpolateNearestNeighbor, coefficients)

  // no need to re-interpolate if the gp didn't change
  override def copy(coefficients: DenseVector[Float]): DiscreteLowRankGpPointTransformation = new DiscreteLowRankGpPointTransformation(dgp, gp, coefficients)
}

object DiscreteLowRankGpPointTransformation {
  def apply(dgp: DiscreteLowRankGaussianProcess[_3D, _3D]): DiscreteLowRankGpPointTransformation = apply(dgp, DenseVector.zeros[Float](dgp.rank))

  def apply(dgp: DiscreteLowRankGaussianProcess[_3D, _3D], coefficients: DenseVector[Float]): DiscreteLowRankGpPointTransformation = new DiscreteLowRankGpPointTransformation(dgp, coefficients)
}

