package scalismo.ui.model

import scalismo.ui.event.Event
import scalismo.ui.model.capabilities.Removeable

object TransformationsNode {

  object event {

    case class TransformationsChanged(source: TransformationsNode) extends Event

  }

}

class TransformationsNode(override val parent: GroupNode) extends SceneNodeCollection[TransformationNode[_]] {
  override val name: String = "Transformations"

  def add[T <: PointTransformation](transformation: T, name: String): TransformationNode[T] = {
    val node = TransformationNode(this, transformation, name)
    add(node)
    node
  }

  override protected def add(child: TransformationNode[_]): Unit = {
    listenTo(child)
    super.add(child)
    publishEvent(TransformationsNode.event.TransformationsChanged(this))
  }

  override def remove(child: TransformationNode[_]): Unit = {
    deafTo(child)
    super.remove(child)
    publishEvent(TransformationsNode.event.TransformationsChanged(this))
  }

  def combinedTransformation: PointTransformation = {
    val transforms = children.map(_.transformation.asInstanceOf[PointTransformation])
    transforms.foldLeft(PointTransformation.Identity: PointTransformation) { case (first, second) => first andThen second }
  }

  reactions += {
    case TransformationNode.event.TransformationChanged(_) =>
      publishEvent(TransformationsNode.event.TransformationsChanged(this))
  }
}

object TransformationNode {
  def apply[T <: PointTransformation](parent: TransformationsNode, transformation: T, name: String): TransformationNode[T] = {
    new TransformationNode(parent, transformation, name)
  }

  object event {

    case class TransformationChanged[T <: PointTransformation](source: TransformationNode[T]) extends Event

  }

}

class TransformationNode[T <: PointTransformation](override val parent: TransformationsNode, initialTransformation: T, override val name: String) extends SceneNode with Removeable {
  private var _transformation: T = initialTransformation

  def transformation: T = _transformation

  def transformation_=(newTransformation: T): Unit = {
    _transformation = newTransformation
    publishEvent(TransformationNode.event.TransformationChanged(this))
  }

  override def remove(): Unit = parent.remove(this)
}
