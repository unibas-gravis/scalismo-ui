package org.statismo.stk.ui

import org.statismo.stk.core.geometry.Point3D

trait Landmarkable {
  def addLandmarkAt(point: Point3D, name : Option[String] = None)
}