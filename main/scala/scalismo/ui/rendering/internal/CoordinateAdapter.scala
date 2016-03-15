package scalismo.ui.rendering.internal

import java.awt.{ Point, Component }

import scalismo.ui.rendering.internal.CoordinateAdapter.VtkPoint

object CoordinateAdapter {
  class VtkPoint(x: Int, y: Int) extends Point(x, y)
}

class CoordinateAdapter {
  var scale: Int = 1
  var height: Int = 0

  def setSize(width: Int, height: Int, panel: Component): Unit = {
    scale = height / panel.getSize.height
    this.height = height
  }

  def toVtkPoint(awtPoint: Point): VtkPoint = {
    val x = awtPoint.x * scale
    val y = height - (awtPoint.y * scale) - 1
    new VtkPoint(x, y)
  }
}
