package scalismo.ui.rendering.util

import java.awt.Color

import scalismo.ui.model.BoundingBox

object VtkUtil {

  def bounds2BoundingBox(bounds: Array[Double]): BoundingBox = {
    val f = bounds.map(f => f.toFloat)
    BoundingBox(f(0), f(1), f(2), f(3), f(4), f(5))
  }

  def colorToArray(color: Color): Array[Double] = {
    color.getRGBColorComponents(null).map(_.toDouble)
  }
}
