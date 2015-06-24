package scalismo.ui

import scalismo.geometry._
import scalismo.statisticalmodel.NDimensionalNormalDistribution

import scala.swing.event.Event

trait HasUncertainty[D <: Dim] extends EdtPublisher {
  def uncertainty: Uncertainty[D]

  def uncertainty_=(newValue: Uncertainty[D]): Unit
}

object HasUncertainty {

  case class UncertaintyChanged[D <: Dim](source: HasUncertainty[D]) extends Event

  trait SimpleImplementation[D <: Dim] extends HasUncertainty[D] {
    // FIXME: This needs to be fixed if we ever support uncertainties in dimensions other than 3D
    private var _uncertainty: Uncertainty[D] = Uncertainty.defaultUncertainty3D().asInstanceOf[Uncertainty[D]]

    override def uncertainty: Uncertainty[D] = _uncertainty

    override def uncertainty_=(u: Uncertainty[D]) = {
      _uncertainty = u
      publishEdt(HasUncertainty.UncertaintyChanged(this))
    }
  }

}

case class Uncertainty[D <: Dim: NDSpace](rotationMatrix: SquareMatrix[D], stdDevs: Vector[D]) {
  lazy val axes: List[Vector[D]] = Uncertainty.Util.matrixToAxes(rotationMatrix)
}

object Uncertainty extends EdtPublisher {

  case object DefaultStdDevs3DChanged extends Event

  def toNDimensionalNormalDistribution[D <: Dim: NDSpace](in: Uncertainty[D]): NDimensionalNormalDistribution[D] = {
    val dim = implicitly[NDSpace[D]].dimensionality
    val variances: Seq[Float] = in.stdDevs.data.map(f => f * f)
    val mean: Vector[D] = Vector(Array.fill(dim)(0.0f))
    NDimensionalNormalDistribution(mean, in.axes.zip(variances))
  }

  def fromNDimensionalNormalDistribution[D <: Dim: NDSpace](in: NDimensionalNormalDistribution[D]): Uncertainty[D] = {
    val (axes, stdDevs) = Util.normalize(in.principalComponents.toList.map { case (a, v) => (a, Math.sqrt(v).toFloat) }).unzip

    Uncertainty[D](Util.axesToMatrix(axes), Vector(stdDevs.toArray))
  }

  private var _defaultStdDevs3D: Vector[_3D] = Vector(1.0f, 1.0f, 1.0f)

  def defaultStdDevs3D: Vector[_3D] = _defaultStdDevs3D

  def defaultStdDevs3D_=(newValue: Vector[_3D]) = {
    if (_defaultStdDevs3D != newValue) {
      _defaultStdDevs3D = newValue
      publishEdt(DefaultStdDevs3DChanged)
    }
  }

  def defaultUncertainty3D(): Uncertainty[_3D] = {
    Uncertainty(Util.I3, _defaultStdDevs3D)
  }

  object Util {
    lazy val I3 = SquareMatrix.eye[_3D]
    lazy val X3 = Vector(1, 0, 0)
    lazy val Y3 = Vector(0, 1, 0)
    lazy val Z3 = Vector(0, 0, 1)

    private lazy val M90 = Math.toRadians(-90)
    private lazy val EqThreshold = 1e-6

    def axesToMatrix[D <: Dim: NDSpace](axes: List[Vector[D]]): SquareMatrix[D] = SquareMatrix(axes.flatMap(_.data).toArray).t

    def matrixToAxes[D <: Dim: NDSpace](matrix: SquareMatrix[D]): List[Vector[D]] = {
      val dim = implicitly[NDSpace[D]].dimensionality
      val list = List.fill(dim)(Array.fill(dim)(0.0f))
      for (row <- 0 until dim; col <- 0 until dim) {
        list(row)(col) = matrix(col, row)
      }
      list.map(a => Vector(a))
    }

    def rotationMatrixFor(fromVector: Vector[_3D], toVector: Vector[_3D]): SquareMatrix[_3D] = {
      val a = fromVector * (1 / fromVector.norm)
      val b = toVector * (1 / toVector.norm)

      val v = Vector.crossproduct(a, b)
      val s = v.norm
      val c = a.dot(b)

      val vx = crossProductMatrix(v)
      I3 + vx + ((vx * vx) * ((1 - c) / (s * s)))
    }

    /**
     * Determine the rotation matrix for rotating around a given axis by a given angle.
     * @param axis The axis to rotate around, given as a unit vector
     * @param angle the angle to rotate, in radians
     * @return the corresponding rotation matrix
     */
    def rotationMatrixFor(axis: Vector[_3D], angle: Double): SquareMatrix[_3D] = {
      val cos = Math.cos(angle)
      (I3 * cos) + (crossProductMatrix(axis) * Math.sin(angle)) + (axis.outer(axis) * (1 - cos))
    }

    def crossProductMatrix(v: Vector[_3D]): SquareMatrix[_3D] = {
      SquareMatrix(
        (0, -v(2), v(1)),
        (v(2), 0, -v(0)),
        (-v(1), v(0), 0))
    }

    /**
     * Normalizes uncertainty principal components, such that the axes are unit vectors in a defined order
     * (i.e. suitable for a rotation matrix).
     * @param in principal components (orthogonal axes and the corresponding uncertainty standard deviations)
     * @tparam D dimensionality
     * @return normalized principal components
     */
    def normalize[D <: Dim: NDSpace](in: List[(Vector[D], Float)]): List[(Vector[D], Float)] = {
      val dim = implicitly[NDSpace[D]].dimensionality
      require(in.length == dim)

      val units = in.map { case (v, f) => (v * (1 / v.norm), f) }
      require(allOrthogonal(units.map(_._1)))

      dim match {
        case 1 => units
        case 2 => reorder2d(units)
        case 3 => reorder3d(units)
      }
    }

    private def allOrthogonal[D <: Dim: NDSpace](v: List[Vector[D]]): Boolean = {

      def ortho(v1: Vector[D], v2: Vector[D]): Boolean = v1.dot(v2) <= EqThreshold

      v.length match {
        case 1 => true
        case 2 => ortho(v(0), v(1))
        case 3 => ortho(v(0), v(1)) && ortho(v(0), v(2)) && ortho(v(1), v(2))
      }
    }

    private def reorder2d[D <: Dim: NDSpace](in: List[(Vector[D], Float)]): List[(Vector[D], Float)] = {
      throw new NotImplementedError("implementation to be done.")
    }

    private def reorder3d[D <: Dim: NDSpace](in: List[(Vector[D], Float)]): List[(Vector[D], Float)] = {
      // simple case: we're using the standard (x,y,z) axes
      if (List(X3, Y3, Z3) == in.map(_._1)) in
      else {
        // first, reorder everything by standard deviation (smallest first)
        val xyz = in.sortBy(_._2).asInstanceOf[List[(Vector[_3D], Float)]]

        // find the first axis, and call it x (2nd and 3rd will be y,z)
        val x = {
          // try to be lucky and end up in the standard orthonormal space
          val x = xyz map (_._1) indexOf X3
          if (x != -1) x else 0
        }

        val (yz, _) = xyz.zipWithIndex.filter(_._2 != x).unzip
        // x rotated around y by -90 degrees gives the z axis. The question is just which of the remaining two axes is y, and which is z.
        val y = if (almostEqual(rotate3d(xyz(x)._1, yz(0)._1, M90), yz(1)._1)) 0 else 1
        val z = 1 - y

        List(xyz(x), yz(y), yz(z)).asInstanceOf[List[(Vector[D], Float)]]
      }
    }

    private def almostEqual[D <: Dim: NDSpace](v1: Vector[D], v2: Vector[D]): Boolean = (v1 - v2).norm <= EqThreshold

    // rotate a point around a line (specified using a vector) by a given angle. The axis is expected to be a unit vector.
    private def rotate3d(point: Vector[_3D], axis: Vector[_3D], alpha: Double) = {
      val (x, y, z) = (point(0), point(1), point(2))
      val (u, v, w) = (axis(0), axis(1), axis(2))
      val (sin, cos) = (Math.sin(alpha), Math.cos(alpha))

      val rx = (u * (u * x + v * y + w * z) * (1 - cos) + x * cos + (-w * y + v * z) * sin).toFloat
      val ry = (v * (u * x + v * y + w * z) * (1 - cos) + y * cos + (w * x - u * z) * sin).toFloat
      val rz = (w * (u * x + v * y + w * z) * (1 - cos) + z * cos + (-v * x + u * y) * sin).toFloat
      Vector(rx, ry, rz)
    }

  }

}

