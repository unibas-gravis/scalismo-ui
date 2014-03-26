package org.statismo.stk.ui

object BoundingBox {
  val Zero = BoundingBox(0,0,0,0,0,0)
}

case class BoundingBox(xMin: Float, xMax: Float, yMin: Float, yMax: Float, zMin: Float, zMax: Float) {
  def union(that: BoundingBox): BoundingBox = {
    BoundingBox(Math.min(this.xMin, that.xMin), Math.max(this.xMax, that.xMax), Math.min(this.yMin, that.yMin), Math.max(this.yMax, that.yMax), Math.min(this.zMin, that.zMin), Math.max(this.zMax, that.zMax))
  }
}
