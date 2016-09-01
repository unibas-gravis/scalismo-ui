package scalismo.ui.model

import scalismo.geometry.{ Point, _3D }

/**
 * Represents a bounding box of a 3D object.
 *
 * The special singleton BoundingBox.Invalid represents an invalid bounding box (e.g. when there are no objects in a viewport).
 *
 */
sealed trait BoundingBox {
  def xMin: Double

  def xMax: Double

  def yMin: Double

  def yMax: Double

  def zMin: Double

  def zMax: Double

  def union(that: BoundingBox): BoundingBox

  def contains(point: Point[_3D]): Boolean

  def center: Point[_3D]
}

object BoundingBox {

  case object Invalid extends BoundingBox {
    override def xMin = 0

    override def xMax = 0

    override def yMin = 0

    override def yMax = 0

    override def zMin = 0

    override def zMax = 0

    override def center: Point[_3D] = Point(0, 0, 0)

    override def contains(point: Point[_3D]): Boolean = false

    override def union(that: BoundingBox): BoundingBox = that
  }

  case class Valid private[BoundingBox] (xMin: Double, xMax: Double, yMin: Double, yMax: Double, zMin: Double, zMax: Double) extends BoundingBox {

    override def union(that: BoundingBox): BoundingBox = {
      if (that == Invalid) this
      else {
        BoundingBox(Math.min(this.xMin, that.xMin), Math.max(this.xMax, that.xMax), Math.min(this.yMin, that.yMin), Math.max(this.yMax, that.yMax), Math.min(this.zMin, that.zMin), Math.max(this.zMax, that.zMax))
      }
    }

    override def contains(point: Point[_3D]): Boolean = {
      xMin <= point(0) && xMax >= point(0) && yMin <= point(1) && yMax >= point(1) && zMin <= point(2) && zMax >= point(2)
    }

    override def center: Point[_3D] = Point((xMin + xMax) / 2, (yMin + yMax) / 2, (zMin + zMax) / 2)

    override def toString: String = {
      s"BoundingBox ($xMin -> $xMax)($yMin -> $yMax)($zMin -> $zMax)"
    }
  }

  def apply(xMin: Double, xMax: Double, yMin: Double, yMax: Double, zMin: Double, zMax: Double): BoundingBox = {
    if (xMin > xMax || yMin > yMax || zMin > zMax) Invalid else Valid(xMin, xMax, yMin, yMax, zMin, zMax)
  }
}

