package scalismo.ui.model.capabilities

import scalismo.geometry.Point3D

trait InverseTransformation {
  def inverseTransform(point: Point3D): Point3D
}
