package scalismo.ui

import scalismo.geometry.{ Point, _3D }

trait Landmarkable {
  def addLandmarkAt(point: Point[_3D], name: Option[String] = None): Unit
}