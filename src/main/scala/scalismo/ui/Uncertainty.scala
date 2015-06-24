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
    val (axes, stdDevs) = in.principalComponents.toList.map {case (a,v) => (a, Math.sqrt(v).toFloat)}.unzip

    val m: SquareMatrix[D] = {
      val candidate = SquareMatrix(axes.flatMap(_.data).toArray)
      if (breeze.linalg.det(candidate.toBreezeMatrix) < 0) {
        // improper rotation matrix
        SquareMatrix(candidate.data.map{f => -f})
      } else candidate
    }

    Uncertainty[D](m, Vector(stdDevs.toArray))
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

    def axesToMatrix[D <: Dim: NDSpace](axes: List[Vector[D]]): SquareMatrix[D] = SquareMatrix(axes.flatMap(_.data).toArray).t

    def matrixToAxes[D <: Dim: NDSpace](matrix: SquareMatrix[D]): List[Vector[D]] = {
      val dim = implicitly[NDSpace[D]].dimensionality
      val list = List.fill(dim)(Array.fill(dim)(0.0f))
      for (row <- 0 until dim; col <- 0 until dim) {
        list(row)(col) = matrix(col, row)
      }
      list.map(a => Vector(a))
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

    def rotationMatrixFor(fromVector: Vector[_3D], toVector: Vector[_3D]): SquareMatrix[_3D] = {
      val a = fromVector * (1 / fromVector.norm)
      val b = toVector * (1 / toVector.norm)

      val v = Vector.crossproduct(a, b)
      val s = v.norm
      val c = a.dot(b)

      val vx = crossProductMatrix(v)
      I3 + vx + ((vx * vx) * ((1 - c) / (s * s)))
    }


    def crossProductMatrix(v: Vector[_3D]): SquareMatrix[_3D] = {
      SquareMatrix(
        (0, -v(2), v(1)),
        (v(2), 0, -v(0)),
        (-v(1), v(0), 0))
    }

  }

}

