package scalismo.ui.rendering.util

import scalismo.ui.model.BoundingBox

object BoundingBoxUtil {
  def bounds2BoundingBox(bounds: Array[Double]): BoundingBox = {
    val f = bounds.map(f => f.toFloat)
    BoundingBox(f(0), f(1), f(2), f(3), f(4), f(5))
  }
}
