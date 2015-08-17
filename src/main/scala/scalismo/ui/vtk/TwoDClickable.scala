package scalismo.ui.vtk

import scalismo.geometry.{ _3D, Point }

trait TwoDClickable {
  def findClosestPoint(clicked: Point[_3D]): Option[Point[_3D]]
  def clicked(point: Point[_3D]): Unit
  def setHighlight(toggled: Boolean)
}
