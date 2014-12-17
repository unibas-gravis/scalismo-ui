package org.statismo.stk.ui

import org.statismo.stk.core.geometry.{Point, _3D}

trait Landmarkable {
  def addLandmarkAt(point: Point[_3D], name : Option[String] = None)
}