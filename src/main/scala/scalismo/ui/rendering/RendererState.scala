package scalismo.ui.rendering

import java.awt.Point

import scalismo.geometry.Point3D
import scalismo.ui.model.SceneNode
import scalismo.ui.rendering.RendererState.PointAndNode

object RendererState {

  case class PointAndNode(pointOption: Option[Point3D], nodeOption: Option[SceneNode])

}

trait RendererState {
  def pointAndNodeAtPosition(point: Point): PointAndNode

  def isHighlightable(node: SceneNode): Boolean

  def setHighlighted(node: SceneNode, onOff: Boolean): Unit
}
