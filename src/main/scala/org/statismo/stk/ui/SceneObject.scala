package org.statismo.stk.ui

import scala.swing.event.Event
import scala.swing.Publisher
import org.statismo.stk.core.mesh.TriangleMesh
import java.awt.Color

//object SceneObject {
//  case object GeometryChanged extends Event
//}

trait SceneObject extends Publisher {
  def displayName: String
  override def toString(): String = {
    displayName
  }
}

object Colorable {
  case class AppearanceChanged(source: Colorable) extends Event
}

trait Colorable extends SceneObject {
  private var _color: Color = Color.WHITE
  
  def color = {_color}
  def color_=(newColor: Color) = {
    if (_color != newColor) {
      _color = newColor
      publish(Colorable.AppearanceChanged(this))
    }
  }
  
  private var _opacity: Double = 1.0
  def opacity = {_opacity}
  def opacity_=(newOpacity: Double) = {
    if (_opacity != newOpacity) {
      _opacity = newOpacity
      publish(Colorable.AppearanceChanged(this))
    }
  }
}

object Surface {
  case class GeometryChanged(source: Surface) extends Event
}

trait Surface extends SceneObject with Colorable {
  def mesh: TriangleMesh
}
