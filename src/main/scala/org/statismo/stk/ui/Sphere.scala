package org.statismo.stk.ui

import org.statismo.stk.core.geometry.Point3D
import scala.swing.event.Event

object Sphere {
  case class GeometryChanged(source: Sphere) extends Event
}

case class Sphere(center: Point3D, initialRadius: Double = 5.0, initialName: String = "(no name)", initialParent: Option[SceneTreeObjectContainer[Displayable]] = None)(implicit override val scene: Scene) extends Displayable with Colorable {
	val radius = initialRadius
	name = initialName
	override lazy val parent = initialParent.getOrElse(scene.auxiliaries)
	parent.add(Seq(this))
}