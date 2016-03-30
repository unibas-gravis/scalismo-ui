package scalismo.ui.model.capabilities

import scalismo.geometry.Point3D

/**
 * Indicates that this node can perform
 * an inverse transformation from a point in the transformed
 * space to the original space.
 */
trait InverseTransformation {
  def inverseTransform(point: Point3D): Point3D
}
