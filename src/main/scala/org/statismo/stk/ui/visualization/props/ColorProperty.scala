package org.statismo.stk.ui.visualization.props

import org.statismo.stk.ui.visualization.VisualizationProperty
import java.awt.Color

class ColorProperty extends VisualizationProperty[Color, ColorProperty]{
  def newInstance() = new ColorProperty
  lazy val defaultValue = Color.WHITE
}
