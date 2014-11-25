package org.statismo.stk.ui.visualization.props

import org.statismo.stk.ui.visualization.VisualizationProperty

class RadiusProperty(initial: Option[Float]) extends VisualizationProperty[Float, RadiusProperty] {
  override def newInstance() = new RadiusProperty(None)

  override def defaultValue = 1.0f

  initial.map(c => value = c)
}

trait HasRadius {
  def radius: RadiusProperty
}