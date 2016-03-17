package scalismo.ui.model

import breeze.linalg.DenseVector
import scalismo.geometry.{ Point, _3D }
import scalismo.registration.{ RigidTransformation, RigidTransformationSpace }
import scalismo.statisticalmodel.LowRankGaussianProcess

/**
  * The general PointTransformation type is simply an alias / another name for
  * "a function that can transform 3D points to other 3D points".
\  */
object PointTransformation {
  val Identity: RigidTransformation[_3D] = RigidTransformationSpace[_3D]().transformForParameters(RigidTransformationSpace[_3D]().identityTransformParameters)

  case class LowRankGpPointTransformation private (gp: LowRankGaussianProcess[_3D, _3D], coefficients: DenseVector[Float]) extends PointTransformation {

    private def this(gp: LowRankGaussianProcess[_3D, _3D]) = this(gp, DenseVector.zeros[Float](gp.rank))

    lazy val vectorField = gp.instance(coefficients)

    override def apply(point: Point[_3D]): Point[_3D] = {
      point + vectorField(point)
    }
  }

  object LowRankGpPointTransformation {
    def apply(gp: LowRankGaussianProcess[_3D, _3D]): LowRankGpPointTransformation = new LowRankGpPointTransformation(gp)
  }

}
