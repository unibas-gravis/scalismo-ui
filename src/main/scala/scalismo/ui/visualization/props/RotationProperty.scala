package scalismo.ui.visualization.props

import scalismo.geometry.{ SquareMatrix, _3D }
import scalismo.ui.visualization.VisualizationProperty

class RotationProperty(initial: Option[SquareMatrix[_3D]]) extends VisualizationProperty[Option[SquareMatrix[_3D]], RotationProperty] {
  lazy val defaultValue: Option[SquareMatrix[_3D]] = None

  override def newInstance(): RotationProperty = new RotationProperty(None)

  value = initial
}

trait HasRotation {
  def rotation: RotationProperty
}