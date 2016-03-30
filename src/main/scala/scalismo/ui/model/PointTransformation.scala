package scalismo.ui.model

import breeze.linalg.DenseVector
import scalismo.geometry.{ Point, _3D }
import scalismo.registration.{ RigidTransformation, RigidTransformationSpace }
import scalismo.statisticalmodel.{ DiscreteLowRankGaussianProcess, LowRankGaussianProcess }

/**
 * The general PointTransformation type is simply an alias / another name for
 * "a function that can transform 3D points to other 3D points".
 */
object PointTransformation {
  val RigidIdentity: RigidTransformation[_3D] = RigidTransformationSpace[_3D]().transformForParameters(RigidTransformationSpace[_3D]().identityTransformParameters)

  val Identity: PointTransformation = { p => p }

}
