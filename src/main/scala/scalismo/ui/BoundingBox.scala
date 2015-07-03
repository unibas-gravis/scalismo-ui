package scalismo.ui

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

  override def toString: String = {
    s"BoundingBox ($xMin -> $xMax)($yMin -> $yMax)($zMin -> $zMax) (dummy: $isDummy)"
  }
}
