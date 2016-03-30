package scalismo.ui.model.capabilities

import scalismo.ui.event.Event
import scalismo.ui.model.{ PointTransformation, TransformationsNode }

object Transformable {

  object event {

    case class GeometryChanged[T](source: Transformable[T]) extends Event

  }

}

trait Transformable[T] extends RenderableSceneNode with Grouped {
  def source: T // the untransformed T

  private def transformationsNode: TransformationsNode = group.transformations

  private var _transformedSource = transform(source, transformationsNode.combinedTransformation)

  def transformedSource: T = _transformedSource

  def transform(untransformed: T, transformation: PointTransformation): T

  def updateTransformedSource(): Unit = {
    _transformedSource = transform(source, transformationsNode.combinedTransformation)
    publishEvent(Transformable.event.GeometryChanged(this))
  }

  listenTo(transformationsNode)

  reactions += {
    case TransformationsNode.event.TransformationsChanged(_) => updateTransformedSource()
  }
}
