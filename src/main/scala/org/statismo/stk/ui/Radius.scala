package org.statismo.stk.ui

import scala.swing.Publisher
import org.statismo.stk.core.geometry.Point3D
import scala.swing.event.Event

object Radius {
  val DefaultRadius = 5.0f
  case class RadiusChanged(source: Radius) extends Event
}

trait Radius extends Publisher {
  
	private var _radius: Float = Radius.DefaultRadius
	def radius = {_radius}
	def radius_=(newRadius: Float) = {
	  if (newRadius != _radius) {
	    _radius = newRadius
	    publish(Radius.RadiusChanged(this))
	  }
	}
}