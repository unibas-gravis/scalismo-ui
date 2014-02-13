package org.statismo.stk.ui

import org.statismo.stk.core.geometry.Point3D

class Sphere(initialCenter: Point3D, radius: Float = 0, name: Option[String] = None, parentOption: Option[SceneTreeObjectContainer[Displayable]] = None)(implicit override val scene: Scene) extends Displayable with SphereLike {
  override lazy val parent = parentOption.getOrElse(scene.auxiliaryObjects)
  radius_=(radius)
  name_=(name.getOrElse(Nameable.NoName))
  parent.add(this)
}