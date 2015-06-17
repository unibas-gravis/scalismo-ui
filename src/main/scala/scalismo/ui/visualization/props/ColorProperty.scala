package scalismo.ui.visualization.props

import java.awt.Color

import scalismo.ui.visualization.VisualizationProperty

class ColorProperty(initial: Option[Color]) extends VisualizationProperty[Color, ColorProperty] {
  override lazy val defaultValue = Color.WHITE

  override protected def newInstance() = new ColorProperty(None)

  initial.foreach(i => value = i)
}

trait HasColor {
  def color: ColorProperty
}

trait HasColorAndOpacity extends HasColor with HasOpacity

