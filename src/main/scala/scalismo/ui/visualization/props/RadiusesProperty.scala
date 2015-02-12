package scalismo.ui.visualization.props

import scalismo.geometry.{ Dim, NDSpace, Vector }
import scalismo.ui.visualization.VisualizationProperty

class RadiusesProperty[D <: Dim: NDSpace](initial: Option[Vector[D]]) extends VisualizationProperty[Vector[D], RadiusesProperty[D]] {
  override def newInstance() = new RadiusesProperty[D](None)

  override def defaultValue = Vector(Array.fill(implicitly[NDSpace[D]].dimensionality)(1.0f))

  initial.map(c => value = c)

  def setAll(v: Float) = {
    value = Vector(Array.fill(implicitly[NDSpace[D]].dimensionality)(v))
  }
}

trait HasRadiuses[D <: Dim] {
  def radiuses: RadiusesProperty[D]
}