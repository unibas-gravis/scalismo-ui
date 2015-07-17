package scalismo.ui

import scalismo.geometry.{ _3D, Point }

object BoundingBox {

  private class DummyBoundingBox extends BoundingBox(0, 0, 0, 0, 0, 0, true)

  val None: BoundingBox = new DummyBoundingBox

  def apply(xMin: Float, xMax: Float, yMin: Float, yMax: Float, zMin: Float, zMax: Float): BoundingBox = {
    if (xMin > xMax || yMin > yMax && zMin > zMax) None else new BoundingBox(xMin, xMax, yMin, yMax, zMin, zMax, false)
  }
}

case class BoundingBox private (xMin: Float, xMax: Float, yMin: Float, yMax: Float, zMin: Float, zMax: Float, isDummy: Boolean) {

  def union(that: BoundingBox): BoundingBox = {
    if (this.isDummy) that
    else if (that.isDummy) this
    else {
      BoundingBox(Math.min(this.xMin, that.xMin), Math.max(this.xMax, that.xMax), Math.min(this.yMin, that.yMin), Math.max(this.yMax, that.yMax), Math.min(this.zMin, that.zMin), Math.max(this.zMax, that.zMax))
    }
  }

  def contains(point: Point[_3D]): Boolean = {
    if (this.isDummy) false else {
      xMin <= point(0) && xMax >= point(0) && yMin <= point(1) && yMax >= point(1) && zMin <= point(2) && zMax >= point(2)
    }
  }

  override def toString: String = {
    s"BoundingBox ($xMin -> $xMax)($yMin -> $yMax)($zMin -> $zMax) (dummy: $isDummy)"
  }
}
