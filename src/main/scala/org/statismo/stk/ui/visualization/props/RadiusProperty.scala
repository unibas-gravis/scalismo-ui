package org.statismo.stk.ui.visualization.props

import org.statismo.stk.ui.visualization.VisualizationProperty

class RadiusProperty extends VisualizationProperty[Float, RadiusProperty]{
  override def newInstance() = new RadiusProperty
  override def defaultValue = 3
}

trait HasRadius {
  def radius: RadiusProperty
}