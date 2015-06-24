package scalismo.ui.visualization.props

import scalismo.geometry.{ Dim, NDSpace, Vector }
import scalismo.ui.visualization.VisualizationProperty

class RadiusesProperty[D <: Dim: NDSpace](initial: Option[Vector[D]]) extends VisualizationProperty[Vector[D], RadiusesProperty[D]] {
  override def newInstance() = new RadiusesProperty[D](None)

  override val defaultValue = Vector(Array.fill(implicitly[NDSpace[D]].dimensionality)(1.0f))

  initial.foreach(i => value = i)

  // this is a workaround for type erasure
  protected[ui] def dimensionality = implicitly[NDSpace[D]].dimensionality

  override protected def sanitizeValue(newValue: Vector[D]): Vector[D] = {
    if (isInsane(newValue)) {
      val array = newValue.data.clone()
      (0 until dimensionality).foreach { i =>
        array(i) = Math.max(0, array(i))
      }
      Vector(array)
    } else newValue
  }

  private def isInsane(v: Vector[D]): Boolean = {
    (0 until dimensionality).foreach { i =>
      if (v(i) < 0) return true
    }
    false
  }
}

trait HasRadiuses[D <: Dim] {
  def radiuses: RadiusesProperty[D]
}
