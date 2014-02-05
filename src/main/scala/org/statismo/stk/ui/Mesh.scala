package org.statismo.stk.ui

import scala.swing.event.Event
import org.statismo.stk.core.mesh.TriangleMesh


object Mesh {
  case class GeometryChanged(source: Mesh) extends Event
}

trait Mesh extends ThreeDRepresentation with Colorable with Landmarkable {
	def triangleMesh: TriangleMesh 
}