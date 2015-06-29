package scalismo.ui.visualization.props

import java.awt.Color

import scalismo.ui.visualization.VisualizationProperty

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

trait HasColor {
  def color: ColorProperty
}

trait HasOpacity {
  def opacity: OpacityProperty
}

trait HasColorAndOpacity extends HasColor with HasOpacity

