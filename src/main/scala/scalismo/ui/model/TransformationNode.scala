package scalismo.ui.model

import scalismo.geometry._3D
import scalismo.registration.RigidTransformation
import scalismo.ui.event.Event
import scalismo.ui.model.capabilities.{ Grouped, Removeable }

import scala.util.{ Failure, Success, Try }

object GenericTransformationsNode {

  object event {

    case class TransformationsChanged(source: TransformationCollectionNode) extends Event

  }

}

object ShapeModelTransformationsNode {

  object event {

    case class ShapeModelTransformationsChanged(source: ShapeModelTransformationsNode) extends Event

  }

}

trait TransformationCollectionNode extends SceneNodeCollection[TransformationNode[_]] {

  val parent: GroupNode

  override protected def add(child: TransformationNode[_]): Unit = {
    listenTo(child)
    super.addToFront(child)
  }

}

class GenericTransformationsNode(override val parent: GroupNode) extends TransformationCollectionNode {
  override val name: String = "Generic transformations"

  def add[T <: PointTransformation](transformation: T, name: String): TransformationNode[T] = {
    val node = TransformationNode(this, transformation, name)
    add(node)
    node
  }

  def combinedTransformation: PointTransformation = {
    val transforms = children.map(_.transformation.asInstanceOf[PointTransformation])
    transforms.foldLeft(PointTransformation.Identity: PointTransformation) { case (first, second) => first compose second }
  }

  override protected def add(child: TransformationNode[_]): Unit = {
    super.add(child)
    publishEvent(GenericTransformationsNode.event.TransformationsChanged(this))
  }

  override def remove(child: TransformationNode[_]): Unit = {
    super.remove(child)
    publishEvent(GenericTransformationsNode.event.TransformationsChanged(this))
  }

  reactions += {
    case TransformationNode.event.TransformationChanged(_) =>
      publishEvent(GenericTransformationsNode.event.TransformationsChanged(this))
  }
}

class ShapeModelTransformationsNode(override val parent: GroupNode) extends TransformationCollectionNode with Removeable {
  override val name: String = "Shape model transformations"

  private def isPoseDefined(): Boolean = {
    children.exists(tr => tr.transformation.isInstanceOf[RigidTransformation[_3D]])
  }
  private def isShapeDefined(): Boolean = {
    children.exists(tr => tr.transformation.isInstanceOf[DiscreteLowRankGpPointTransformation])
  }

  def addPoseTransformation(transformation: RigidTransformation[_3D], name: String): Try[ShapeModelTransformationComponentNode[RigidTransformation[_3D]]] = {

    if (isPoseDefined) {
      Failure(new Exception("The group already contains a rigid transformation as part of the Shape Model Transformation. Remove existing first"))
    } else {
      val node = ShapeModelTransformationComponentNode(this, transformation, name)
      add(node)
      Success(node)
    }
  }

  def addGaussianProcessTransformation(transformation: DiscreteLowRankGpPointTransformation, name: String): Try[ShapeModelTransformationComponentNode[DiscreteLowRankGpPointTransformation]] = {

    if (isShapeDefined()) {
      Failure(new Exception("The group already contains a GP transformation as part of the Shape Model Transformation. Remove existing first"))
    } else {
      val node = ShapeModelTransformationComponentNode(this, transformation, name)
      add(node)
      Success(node)
    }
  }

  def poseTransformation: Option[ShapeModelTransformationComponentNode[RigidTransformation[_3D]]] =
    children.find(_.transformation.isInstanceOf[RigidTransformation[_3D]]).map(_.asInstanceOf[ShapeModelTransformationComponentNode[RigidTransformation[_3D]]])

  def gaussianProcessTransformation: Option[ShapeModelTransformationComponentNode[DiscreteLowRankGpPointTransformation]] =
    children.find(_.transformation.isInstanceOf[DiscreteLowRankGpPointTransformation]).map(_.asInstanceOf[ShapeModelTransformationComponentNode[DiscreteLowRankGpPointTransformation]])

  protected def add(child: ShapeModelTransformationComponentNode[_]): Unit = {
    listenTo(child)
    super.addToFront(child)
    publishEvent(ShapeModelTransformationsNode.event.ShapeModelTransformationsChanged(this))
  }

  override def remove(child: TransformationNode[_]): Unit = {
    deafTo(child)
    super.remove(child)
    publishEvent(ShapeModelTransformationsNode.event.ShapeModelTransformationsChanged(this))
  }

  def combinedTransformation: Option[PointTransformation] = {
    gaussianProcessTransformation match {
      case Some(shapeTrans) => poseTransformation match {
        case Some(poseTrans) => Some(poseTrans.transformation compose shapeTrans.transformation)
        case None => Some(shapeTrans.transformation)
      }
      case None => poseTransformation match {
        case Some(poseTrans) => Some(poseTrans.transformation)
        case None => None
      }
    }
  }

  // in this case remove does not really remove the node from the parent group, but just empties its children
  def remove(): Unit = {
    children.foreach(_.remove())
  }

  reactions += {
    case TransformationNode.event.TransformationChanged(_) =>
      publishEvent(ShapeModelTransformationsNode.event.ShapeModelTransformationsChanged(this))
  }
}

class ShapeModelTransformationComponentNode[T <: PointTransformation] private (override val parent: ShapeModelTransformationsNode, initialTransformation: T, override val name: String)
    extends TransformationNode[T](parent, initialTransformation, name) {
  override def remove(): Unit = { parent.remove(this) }
}

object ShapeModelTransformationComponentNode {
  def apply(parent: ShapeModelTransformationsNode, initialTransformation: RigidTransformation[_3D], name: String) = new ShapeModelTransformationComponentNode(parent, initialTransformation, name)

  def apply(parent: ShapeModelTransformationsNode, initialTransformation: DiscreteLowRankGpPointTransformation, name: String) = new ShapeModelTransformationComponentNode(parent, initialTransformation, name)
}

object TransformationNode {
  def apply[T <: PointTransformation](parent: TransformationCollectionNode, transformation: T, name: String): TransformationNode[T] = {
    new TransformationNode(parent, transformation, name)
  }

  object event {

    case class TransformationChanged[T <: PointTransformation](source: TransformationNode[T]) extends Event

  }

}

class TransformationNode[T <: PointTransformation](override val parent: TransformationCollectionNode, initialTransformation: T, override val name: String) extends SceneNode with Grouped with Removeable {
  private var _transformation: T = initialTransformation

  def transformation: T = _transformation

  def transformation_=(newTransformation: T): Unit = {
    _transformation = newTransformation
    publishEvent(TransformationNode.event.TransformationChanged(this))
  }

  override def remove(): Unit = parent.remove(this)

  override def group: GroupNode = parent.parent
}

