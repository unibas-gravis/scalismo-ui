package org.statismo.stk.ui.visualization.props

import org.statismo.stk.core.geometry.{_3D, SquareMatrix}
import org.statismo.stk.ui.visualization.VisualizationProperty

class RotationProperty(initial: Option[SquareMatrix[_3D]]) extends VisualizationProperty[Option[SquareMatrix[_3D]], RotationProperty] {
  lazy val defaultValue: Option[SquareMatrix[_3D]] = None

  override def newInstance(): RotationProperty = new RotationProperty(None)

  value = initial
}

trait HasRotation {
  def rotation: RotationProperty
}