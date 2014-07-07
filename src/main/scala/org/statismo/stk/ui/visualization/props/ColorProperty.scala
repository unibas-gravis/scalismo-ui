package org.statismo.stk.ui.visualization.props

import org.statismo.stk.ui.visualization.VisualizationProperty
import java.awt.Color

class ColorProperty(initial: Option[Color]) extends VisualizationProperty[Color, ColorProperty] {
  def newInstance() = new ColorProperty(None)

  lazy val defaultValue = Color.WHITE
  initial.map(c => value = c)
}

class OpacityProperty(initial: Option[Double]) extends VisualizationProperty[Double, OpacityProperty] {
  def newInstance() = new OpacityProperty(None)

  lazy val defaultValue = 1.0d
  initial.map(c => value = c)
}

trait HasColorAndOpacity {
  def color: ColorProperty

  def opacity: OpacityProperty
}

