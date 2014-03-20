package org.statismo.stk.ui.visualization.props

import org.statismo.stk.ui.visualization.VisualizationProperty
import java.awt.Color

class ColorProperty extends VisualizationProperty[Color, ColorProperty] {
  def newInstance() = new ColorProperty
  lazy val defaultValue = Color.WHITE
}

class OpacityProperty extends VisualizationProperty[Double, OpacityProperty] {
  def newInstance() = new OpacityProperty
  lazy val defaultValue = 1.0d
}

trait HasColorAndOpacity {
  def color: ColorProperty
  def opacity: OpacityProperty
}

