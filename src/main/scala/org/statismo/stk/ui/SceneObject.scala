package org.statismo.stk.ui

import scala.swing.event.Event
import scala.swing.Publisher
import org.statismo.stk.core.mesh.TriangleMesh

object SceneObject {
  case object GeometryChanged extends Event
}

trait SceneObject extends Publisher {
  def displayName: String
  override def toString(): String = {
    displayName
  }
}

trait Surface extends SceneObject {
  def mesh: TriangleMesh
}
