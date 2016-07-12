package scalismo.ui.model.capabilities

import scalismo.ui.event.Event
import scalismo.ui.model.{GenericTransformationsNode, PointTransformation, ShapeModelTransformationsNode}

object Transformable {

  object event {

    case class GeometryChanged[T](source: Transformable[T]) extends Event

  }

}

trait Transformable[T] extends RenderableSceneNode with Grouped {
  def source: T // the untransformed T

  private def genericTransformationsNode: GenericTransformationsNode = group.genericTransformations
  private def shapeModelTransformationsNode: ShapeModelTransformationsNode = group.shapeModelTransformations

  private def combinedTransform = shapeModelTransformationsNode.combinedTransformation.map( smT => genericTransformationsNode.combinedTransformation compose smT) getOrElse{
    genericTransformationsNode.combinedTransformation
  }

  private var _transformedSource = transform(source, combinedTransform)

  def transformedSource: T = _transformedSource

  def transform(untransformed: T, transformation: PointTransformation): T

  def updateTransformedSource(): Unit = {
    _transformedSource = transform(source, combinedTransform)
    publishEvent(Transformable.event.GeometryChanged(this))
  }

  listenTo(genericTransformationsNode)
  listenTo(shapeModelTransformationsNode)

  reactions += {
    case GenericTransformationsNode.event.TransformationsChanged(_) => updateTransformedSource()
    case ShapeModelTransformationsNode.event.ShapeModelTransformationsChanged(_) => updateTransformedSource()
  }
}
