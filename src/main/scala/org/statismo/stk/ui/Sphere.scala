package org.statismo.stk.ui

import org.statismo.stk.core.geometry.Point3D
import scala.swing.event.Event

case class Sphere(initialCenter: Point3D, initialRadius: Float = 0, initialName: String = Nameable.DefaultName, initialParent: Option[SceneTreeObjectContainer[Displayable]] = None)(implicit override val scene: Scene) extends Displayable with SphereLike {
  radius = initialRadius
  name = initialName
  override lazy val parent = initialParent.getOrElse(scene.auxiliaries)
  parent.add(this)
}