package scalismo.ui.api

import java.awt.Color

import breeze.linalg.DenseVector
import scalismo.common.{ DiscreteScalarField, DiscreteVectorField }
import scalismo.geometry.{ Dim, Landmark, Point, Vector, _3D }
import scalismo.image.DiscreteScalarImage
import scalismo.mesh.{ LineMesh, ScalarMeshField, TriangleMesh }
import scalismo.registration.RigidTransformation
import scalismo.statisticalmodel.{ DiscreteLowRankGaussianProcess, StatisticalMeshModel }
import scalismo.ui.control.NodeVisibility
import scalismo.ui.model.SceneNode.event.{ ChildAdded, ChildRemoved }
import scalismo.ui.model._
import scalismo.ui.model.capabilities.{ Removeable, RenderableSceneNode }
import scalismo.ui.model.properties.ScalarRange
import scalismo.ui.view.ScalismoFrame

sealed trait ObjectView {
  type PeerType <: SceneNode with Removeable

  protected[api] def peer: PeerType

  protected[api] val frame: ScalismoFrame

  def name: String = peer.name

  def inGroup: Group = {
    Group(findBelongingGroup(peer), frame)
  }

  def remove(): Unit = peer.remove()

  private def findBelongingGroup(node: SceneNode): GroupNode = {
    if (node.isInstanceOf[GroupNode]) node.asInstanceOf[GroupNode]
    else findBelongingGroup(node.parent)
  }
}

object ObjectView {
  implicit object FindInSceneObjectView extends FindInScene[ObjectView] {
    override def createView(s: SceneNode, _frame: ScalismoFrame): Option[ObjectView] = {

      s match {
        case node: GroupNode => None // we ignore all group nodes, as they are not real objects
        case node: SceneNode with Removeable => {
          val ov = new ObjectView {
            override val frame = _frame
            override type PeerType = SceneNode with Removeable

            override protected[api] def peer = node
          }
          Some(ov)
        }
        case _ => None
      }
    }
  }
}

case class PointCloudView private[ui] (override protected[api] val peer: PointCloudNode, frame: ScalismoFrame) extends ObjectView with SimpleVisibility {

  type PeerType = PointCloudNode

  def color = peer.color.value

  def color_=(c: Color): Unit = {
    peer.color.value = c
  }

  def radius = peer.radius.value

  def radius_=(r: Double): Unit = {
    peer.radius.value = r
  }

  def opacity = peer.opacity.value

  def opacity_=(o: Double): Unit = {
    peer.opacity.value = o
  }

  def points: IndexedSeq[Point[_3D]] = peer.source

  def transformedPoints: IndexedSeq[Point[_3D]] = peer.transformedSource
}

object PointCloudView {

  implicit object FindInScenePointCloud extends FindInScene[PointCloudView] {
    override def createView(s: SceneNode, frame: ScalismoFrame): Option[PointCloudView] = {
      s match {
        case node: PointCloudNode => Some(PointCloudView(node, frame))
        case _ => None
      }
    }
  }

  implicit def callbackPointCloudView = new HandleCallback[PointCloudView] {

    override def registerOnAdd[R](g: Group, f: PointCloudView => R, frame: ScalismoFrame): Unit = {
      g.peer.listenTo(g.peer.pointClouds)
      g.peer.reactions += {
        case ChildAdded(collection, newNode: PointCloudNode) =>
          val tmv = PointCloudView(newNode, frame)
          f(tmv)
      }
    }

    override def registerOnRemove[R](g: Group, f: PointCloudView => R, frame: ScalismoFrame): Unit = {
      g.peer.listenTo(g.peer.pointClouds)
      g.peer.reactions += {
        case ChildRemoved(collection, removedNode: PointCloudNode) =>
          val tmv = PointCloudView(removedNode, frame)
          f(tmv)
      }
    }
  }
}

case class TriangleMeshView private[ui] (override protected[api] val peer: TriangleMeshNode, frame: ScalismoFrame) extends ObjectView with SimpleVisibility {
  type PeerType = TriangleMeshNode

  def color = peer.color.value

  def color_=(c: Color): Unit = {
    peer.color.value = c
  }

  def opacity = peer.opacity.value

  def opacity_=(o: Double): Unit = {
    peer.opacity.value = o
  }

  def lineWidth = peer.lineWidth.value

  def lineWidth_=(width: Int): Unit = {
    peer.lineWidth.value = width
  }

  def triangleMesh: TriangleMesh[_3D] = peer.source

  def transformedTriangleMesh: TriangleMesh[_3D] = peer.transformedSource
}

object TriangleMeshView {

  implicit object FindInSceneTriangleMeshView$ extends FindInScene[TriangleMeshView] {
    override def createView(s: SceneNode, frame: ScalismoFrame): Option[TriangleMeshView] = {
      s match {
        case peer: TriangleMeshNode => Some(TriangleMeshView(peer, frame))
        case _ => None
      }
    }
  }

  implicit object callbacksTriangleMeshView extends HandleCallback[TriangleMeshView] {

    override def registerOnAdd[R](g: Group, f: TriangleMeshView => R, frame: ScalismoFrame): Unit = {
      g.peer.listenTo(g.peer.triangleMeshes)
      g.peer.reactions += {
        case ChildAdded(collection, newNode: TriangleMeshNode) =>
          val tmv = TriangleMeshView(newNode, frame)
          f(tmv)
      }
    }

    override def registerOnRemove[R](g: Group, f: TriangleMeshView => R, frame: ScalismoFrame): Unit = {
      g.peer.listenTo(g.peer.triangleMeshes)
      g.peer.reactions += {
        case ChildRemoved(collection, removedNode: TriangleMeshNode) =>
          val tmv = TriangleMeshView(removedNode, frame)
          f(tmv)
      }
    }

  }
}

case class LineMeshView private[ui] (override protected[api] val peer: LineMeshNode, frame: ScalismoFrame) extends ObjectView with SimpleVisibility {
  type PeerType = LineMeshNode

  def color = peer.color.value

  def color_=(c: Color): Unit = {
    peer.color.value = c
  }

  def opacity = peer.opacity.value

  def opacity_=(o: Float): Unit = {
    peer.opacity.value = o
  }

  def lineWidth = peer.lineWidth.value
  def lineWidth_=(width: Int): Unit = {
    peer.lineWidth.value = width
  }

  def lineMesh: LineMesh[_3D] = peer.source

  def transformedLineMesh: LineMesh[_3D] = peer.transformedSource
}

object LineMeshView {

  implicit object FindInSceneLineMeshView extends FindInScene[LineMeshView] {
    override def createView(s: SceneNode, frame: ScalismoFrame): Option[LineMeshView] = {
      s match {
        case peer: LineMeshNode => Some(LineMeshView(peer, frame))
        case _ => None
      }
    }
  }

  implicit object callbackLineMeshView extends HandleCallback[LineMeshView] {

    override def registerOnAdd[R](g: Group, f: LineMeshView => R, frame: ScalismoFrame): Unit = {
      g.peer.listenTo(g.peer.lineMeshes)
      g.peer.reactions += {
        case ChildAdded(collection, newNode: LineMeshNode) =>
          val lmv = LineMeshView(newNode, frame)
          f(lmv)
      }
    }

    override def registerOnRemove[R](g: Group, f: LineMeshView => R, frame: ScalismoFrame): Unit = {
      g.peer.listenTo(g.peer.lineMeshes)
      g.peer.reactions += {
        case ChildRemoved(collection, removedNode: LineMeshNode) =>
          val lmv = LineMeshView(removedNode, frame)
          f(lmv)
      }
    }

  }
}

case class LandmarkView private[ui] (override protected[api] val peer: LandmarkNode, frame: ScalismoFrame) extends ObjectView with SimpleVisibility {
  type PeerType = LandmarkNode

  def color = peer.color.value

  def color_=(c: Color): Unit = {
    peer.color.value = c
  }

  def opacity = peer.opacity.value

  def opacity_=(o: Double): Unit = {
    peer.opacity.value = o
  }

  def landmark: Landmark[_3D] = peer.source

  def transformedLandmark: Landmark[_3D] = peer.transformedSource
}

object LandmarkView {

  implicit object FindInSceneLandmarkView$ extends FindInScene[LandmarkView] {
    override def createView(s: SceneNode, frame: ScalismoFrame): Option[LandmarkView] = {
      s match {
        case peer: LandmarkNode => Some(LandmarkView(peer, frame))
        case _ => None
      }
    }
  }

  implicit object CallbackLandmarkView extends HandleCallback[LandmarkView] {

    override def registerOnAdd[R](g: Group, f: LandmarkView => R, frame: ScalismoFrame): Unit = {
      g.peer.listenTo(g.peer.landmarks)
      g.peer.reactions += {
        case ChildAdded(collection, newNode: LandmarkNode) =>
          val tmv = LandmarkView(newNode, frame)
          f(tmv)
      }
    }

    override def registerOnRemove[R](g: Group, f: LandmarkView => R, frame: ScalismoFrame): Unit = {
      g.peer.listenTo(g.peer.landmarks)
      g.peer.reactions += {
        case ChildRemoved(collection, removedNode: LandmarkNode) =>
          val tmv = LandmarkView(removedNode, frame)
          f(tmv)
      }
    }
  }

}

case class ScalarMeshFieldView private[ui] (override protected[api] val peer: ScalarMeshFieldNode, frame: ScalismoFrame) extends ObjectView with SimpleVisibility {
  type PeerType = ScalarMeshFieldNode

  def scalarRange: ScalarRange = peer.scalarRange.value

  def scalarRange_=(s: ScalarRange): Unit = {
    peer.scalarRange.value = s
  }

  def opacity = peer.opacity.value

  def opacity_=(o: Double): Unit = {
    peer.opacity.value = o
  }

  def lineWidth = peer.lineWidth.value
  def lineWidth_=(width: Int): Unit = {
    peer.lineWidth.value = width
  }

  def scalarMeshField: ScalarMeshField[Float] = peer.source

  def transformedScalarMeshField = peer.transformedSource
}

object ScalarMeshFieldView {

  implicit object FindInSceneScalarMeshField extends FindInScene[ScalarMeshFieldView] {
    override def createView(s: SceneNode, frame: ScalismoFrame): Option[ScalarMeshFieldView] = {
      s match {
        case node: ScalarMeshFieldNode => Some(ScalarMeshFieldView(node, frame))
        case _ => None
      }
    }
  }

  implicit object CallbackScalarMeshFieldView extends HandleCallback[ScalarMeshFieldView] {

    override def registerOnAdd[R](g: Group, f: ScalarMeshFieldView => R, frame: ScalismoFrame): Unit = {
      g.peer.listenTo(g.peer.scalarMeshFields)
      g.peer.reactions += {
        case ChildAdded(collection, newNode: ScalarMeshFieldNode) =>
          val tmv = ScalarMeshFieldView(newNode, frame)
          f(tmv)
      }
    }

    override def registerOnRemove[R](g: Group, f: ScalarMeshFieldView => R, frame: ScalismoFrame): Unit = {
      g.peer.listenTo(g.peer.scalarMeshFields)
      g.peer.reactions += {
        case ChildRemoved(collection, removedNode: ScalarMeshFieldNode) =>
          val tmv = ScalarMeshFieldView(removedNode, frame)
          f(tmv)
      }
    }
  }

}

case class ScalarFieldView private[ui] (override protected[api] val peer: ScalarFieldNode, frame: ScalismoFrame) extends ObjectView with SimpleVisibility {
  type PeerType = ScalarFieldNode

  def scalarRange: ScalarRange = peer.scalarRange.value

  def scalarRange_=(s: ScalarRange): Unit = {
    peer.scalarRange.value = s
  }

  def radius = peer.radius.value

  def radius_=(r: Double): Unit = {
    peer.radius.value = r
  }

  def opacity = peer.opacity.value

  def opacity_=(o: Double): Unit = {
    peer.opacity.value = o
  }

  def scalarField: DiscreteScalarField[_3D, Float] = peer.source

  def transformedScalarField = peer.transformedSource
}

object ScalarFieldView {

  implicit object FindInSceneScalarMeshField extends FindInScene[ScalarFieldView] {
    override def createView(s: SceneNode, frame: ScalismoFrame): Option[ScalarFieldView] = {
      s match {
        case node: ScalarFieldNode => Some(ScalarFieldView(node, frame))
        case _ => None
      }
    }
  }

  implicit object CallbackScalarFieldView extends HandleCallback[ScalarFieldView] {

    override def registerOnAdd[R](g: Group, f: ScalarFieldView => R, frame: ScalismoFrame): Unit = {
      g.peer.listenTo(g.peer.scalarFields)
      g.peer.reactions += {
        case ChildAdded(collection, newNode: ScalarFieldNode) =>
          val tmv = ScalarFieldView(newNode, frame)
          f(tmv)
      }
    }

    override def registerOnRemove[R](g: Group, f: ScalarFieldView => R, frame: ScalismoFrame): Unit = {
      g.peer.listenTo(g.peer.scalarFields)
      g.peer.reactions += {
        case ChildRemoved(collection, removedNode: ScalarFieldNode) =>
          val tmv = ScalarFieldView(removedNode, frame)
          f(tmv)
      }
    }
  }

}

case class VectorFieldView private[ui] (override protected[api] val peer: VectorFieldNode, frame: ScalismoFrame) extends ObjectView with SimpleVisibility {
  type PeerType = VectorFieldNode

  def scalarRange: ScalarRange = peer.scalarRange.value

  def scalarRange_=(s: ScalarRange): Unit = {
    peer.scalarRange.value = s
  }

  def opacity = peer.opacity.value

  def opacity_=(o: Double): Unit = {
    peer.opacity.value = o
  }

  def vectorField[A]: DiscreteVectorField[_3D, _3D] = peer.source
}

object VectorFieldView {

  implicit object FindInSceneScalarMeshField extends FindInScene[VectorFieldView] {
    override def createView(s: SceneNode, frame: ScalismoFrame): Option[VectorFieldView] = {
      s match {
        case node: VectorFieldNode => Some(VectorFieldView(node, frame))
        case _ => None
      }
    }
  }

  implicit object CallbackVectorFieldView extends HandleCallback[VectorFieldView] {

    override def registerOnAdd[R](g: Group, f: VectorFieldView => R, frame: ScalismoFrame): Unit = {
      g.peer.listenTo(g.peer.vectorFields)
      g.peer.reactions += {
        case ChildAdded(collection, newNode: VectorFieldNode) =>
          val tmv = VectorFieldView(newNode, frame)
          f(tmv)
      }
    }

    override def registerOnRemove[R](g: Group, f: VectorFieldView => R, frame: ScalismoFrame): Unit = {
      g.peer.listenTo(g.peer.vectorFields)
      g.peer.reactions += {
        case ChildRemoved(collection, removedNode: VectorFieldNode) =>
          val tmv = VectorFieldView(removedNode, frame)
          f(tmv)
      }
    }
  }

}

case class ImageView private[ui] (override protected[api] val peer: ImageNode, frame: ScalismoFrame) extends ObjectView with SimpleVisibility {
  type PeerType = ImageNode

  def opacity = peer.opacity.value

  def opacity_=(o: Double): Unit = {
    peer.opacity.value = o
  }

  def window: Double = peer.windowLevel.value.window
  def window_=(w: Double): Unit = {
    peer.windowLevel.value = peer.windowLevel.value.copy(window = w)
  }

  def level = peer.windowLevel.value.level
  def level_=(w: Double): Unit = {
    peer.windowLevel.value = peer.windowLevel.value.copy(level = w)
  }

  def image: DiscreteScalarImage[_3D, Float] = peer.source
}

object ImageView {

  implicit object FindImage extends FindInScene[ImageView] {
    override def createView(s: SceneNode, frame: ScalismoFrame): Option[ImageView] = {
      s match {
        case imageNode: ImageNode => Some(ImageView(imageNode, frame))
        case _ => None
      }
    }
  }

  implicit object CallbackLandmarkView extends HandleCallback[ImageView] {

    override def registerOnAdd[R](g: Group, f: ImageView => R, frame: ScalismoFrame): Unit = {
      g.peer.listenTo(g.peer.images)
      g.peer.reactions += {
        case ChildAdded(collection, newNode: ImageNode) =>
          val imv = ImageView(newNode, frame)
          f(imv)
      }
    }

    override def registerOnRemove[R](g: Group, f: ImageView => R, frame: ScalismoFrame): Unit = {
      g.peer.listenTo(g.peer.images)
      g.peer.reactions += {
        case ChildRemoved(collection, removedNode: ImageNode) =>
          val tmv = ImageView(removedNode, frame)
          f(tmv)
      }
    }
  }

}

// Note this class does not extend Object view, as there is not really a corresponding node to this concept
case class StatisticalMeshModelViewControls private[ui] (val meshView: TriangleMeshView, val shapeModelTransformationView: ShapeModelTransformationView)

case class Group(override protected[api] val peer: GroupNode, val frame: ScalismoFrame) extends ObjectView {

  def hidden_=(b: Boolean): Unit = {
    peer.isGhost = b
  }

  def hidden = peer.isGhost

  type PeerType = GroupNode
}

object Group {

  implicit object FindInSceneGroup$ extends FindInScene[Group] {
    override def createView(s: SceneNode, frame: ScalismoFrame): Option[Group] = {
      s match {
        case node: GroupNode => Some(Group(node, frame))
        case _ => None
      }
    }
  }

}

case class TransformationView private[ui] (override protected[api] val peer: TransformationNode[Point[_3D] => Point[_3D]], frame: ScalismoFrame) extends ObjectView {
  def transformation: Point[_3D] => Point[_3D] = peer.transformation

  def transformation_=(t: Point[_3D] => Point[_3D]): Unit = {
    peer.transformation = t
  }

  override type PeerType = TransformationNode[Point[_3D] => Point[_3D]]

}

object TransformationView {

  implicit object FindInSceneGenericTransformation$ extends FindInScene[TransformationView] {
    override def createView(s: SceneNode, frame: ScalismoFrame): Option[TransformationView] = {

      type PointToPointTransformation[D <: Dim] = Point[D] => Point[D]
      // here we need a two step process due to type erasure to find the right type.
      s match {
        case value: TransformationNode[_] if value.transformation.isInstanceOf[PointToPointTransformation[_]] =>
          Some(TransformationView(s.asInstanceOf[TransformationNode[PointToPointTransformation[_3D]]], frame))
        case _ => None
      }
    }
  }

}

case class RigidTransformationView private[ui] (override protected[api] val peer: TransformationNode[RigidTransformation[_3D]], frame: ScalismoFrame) extends ObjectView {

  override type PeerType = TransformationNode[RigidTransformation[_3D]]

  def transformation: RigidTransformation[_3D] = peer.transformation

  def transformation_=(transform: RigidTransformation[_3D]): Unit = {
    peer.transformation = transform
  }
}

object RigidTransformationView {

  implicit object FindInSceneRigidTransformation$ extends FindInScene[RigidTransformationView] {
    override def createView(s: SceneNode, frame: ScalismoFrame): Option[RigidTransformationView] = {

      // here we need a two step process due to type erasure to find the right type.
      s match {
        // filter out Rigid transformations that are part of a StatisticalShapeMoodelTransformation
        case value: ShapeModelTransformationComponentNode[_] if value.transformation.isInstanceOf[RigidTransformation[_]] => None
        case value: TransformationNode[_] if value.transformation.isInstanceOf[RigidTransformation[_]] =>
          Some(RigidTransformationView(s.asInstanceOf[TransformationNode[RigidTransformation[_3D]]], frame))
        case _ => None
      }
    }
  }

  implicit object CallbackRigidTransformation extends HandleCallback[RigidTransformationView] {

    override def registerOnAdd[R](g: Group, f: RigidTransformationView => R, frame: ScalismoFrame): Unit = {
      g.peer.listenTo(g.peer.genericTransformations)
      g.peer.reactions += {
        case ChildAdded(collection, newNode: TransformationNode[_]) =>

          if (newNode.transformation.isInstanceOf[RigidTransformation[_]]) {
            val tmv = RigidTransformationView(newNode.asInstanceOf[TransformationNode[RigidTransformation[_3D]]], frame)
            f(tmv)
          }

      }
    }

    override def registerOnRemove[R](g: Group, f: RigidTransformationView => R, frame: ScalismoFrame): Unit = {
      g.peer.listenTo(g.peer.genericTransformations)
      g.peer.reactions += {
        case ChildRemoved(collection, removedNode: TransformationNode[_]) =>
          if (removedNode.transformation.isInstanceOf[RigidTransformation[_]]) {
            val tmv = RigidTransformationView(removedNode.asInstanceOf[TransformationNode[RigidTransformation[_3D]]], frame)
            f(tmv)
          }
      }
    }
  }

}

case class DiscreteLowRankGPTransformationView private[ui] (override protected[api] val peer: TransformationNode[DiscreteLowRankGpPointTransformation], frame: ScalismoFrame) extends ObjectView {

  override type PeerType = TransformationNode[DiscreteLowRankGpPointTransformation]

  def coefficients: DenseVector[Double] = peer.transformation.coefficients

  def coefficients_=(coefficients: DenseVector[Double]): Unit = {
    {
      peer.transformation = peer.transformation.copy(coefficients)
    }
  }

  val transformation: DiscreteLowRankGpPointTransformation = peer.transformation

  def discreteLowRankGaussianProcess = peer.transformation.dgp

  def discreteLowRankGaussianProcess_=(dgp: DiscreteLowRankGaussianProcess[_3D, Vector[_3D]]): Unit = {
    peer.transformation = DiscreteLowRankGpPointTransformation(dgp)
  }
}

object DiscreteLowRankGPTransformationView {

  implicit object FindInSceneDiscreteGPTransformation$ extends FindInScene[DiscreteLowRankGPTransformationView] {
    override def createView(s: SceneNode, frame: ScalismoFrame): Option[DiscreteLowRankGPTransformationView] = {

      // here we need a two step process due to type erasure to find the right type.
      s match {
        // filter out Rigid transformations that are part of a StatisticalShapeMoodelTransformation
        case value: ShapeModelTransformationComponentNode[_] if value.transformation.isInstanceOf[DiscreteLowRankGpPointTransformation] => None
        case value: TransformationNode[_] if value.transformation.isInstanceOf[DiscreteLowRankGpPointTransformation] =>
          Some(DiscreteLowRankGPTransformationView(s.asInstanceOf[TransformationNode[DiscreteLowRankGpPointTransformation]], frame))
        case _ => None
      }
    }
  }

  implicit object CallbackDiscreteGPTransformation extends HandleCallback[DiscreteLowRankGPTransformationView] {

    override def registerOnAdd[R](g: Group, f: DiscreteLowRankGPTransformationView => R, frame: ScalismoFrame): Unit = {
      g.peer.listenTo(g.peer.genericTransformations)
      g.peer.reactions += {
        case ChildAdded(collection, newNode: TransformationNode[_]) =>

          if (newNode.transformation.isInstanceOf[DiscreteLowRankGpPointTransformation]) {
            val tmv = DiscreteLowRankGPTransformationView(newNode.asInstanceOf[TransformationNode[DiscreteLowRankGpPointTransformation]], frame)
            f(tmv)
          }

      }
    }

    override def registerOnRemove[R](g: Group, f: DiscreteLowRankGPTransformationView => R, frame: ScalismoFrame): Unit = {
      g.peer.listenTo(g.peer.genericTransformations)
      g.peer.reactions += {
        case ChildRemoved(collection, removedNode: TransformationNode[_]) =>
          if (removedNode.transformation.isInstanceOf[DiscreteLowRankGpPointTransformation]) {
            val tmv = DiscreteLowRankGPTransformationView(removedNode.asInstanceOf[TransformationNode[DiscreteLowRankGpPointTransformation]], frame)
            f(tmv)
          }
      }
    }
  }

}

case class LowRankGPTransformationView private[ui] (override protected[api] val peer: TransformationNode[LowRankGpPointTransformation], frame: ScalismoFrame) extends ObjectView {

  override type PeerType = TransformationNode[LowRankGpPointTransformation]

  def coefficients: DenseVector[Double] = peer.transformation.coefficients

  def coefficients_=(coefficients: DenseVector[Double]): Unit = {
    {
      peer.transformation = peer.transformation.copy(coefficients)
    }
  }
}

case class ShapeModelTransformation(poseTransformation: RigidTransformation[_3D], shapeTransformation: DiscreteLowRankGpPointTransformation)

object ShapeModelTransformation {
  def apply(poseTransformation: RigidTransformation[_3D], gp: DiscreteLowRankGaussianProcess[_3D, Vector[_3D]]): ShapeModelTransformation = {
    ShapeModelTransformation(poseTransformation, DiscreteLowRankGpPointTransformation(gp))
  }
}

case class ShapeModelTransformationView private[ui] (override protected[api] val peer: ShapeModelTransformationsNode, frame: ScalismoFrame) extends ObjectView {

  override type PeerType = ShapeModelTransformationsNode

  def shapeTransformationView = peer.gaussianProcessTransformation.map(DiscreteLowRankGPTransformationView(_, frame)) match {
    case Some(sv) => sv
    case None => throw new Exception("There is no Gaussian Process (shape) transformation associated with this ShapeModelTransformationView.")
  }

  def poseTransformationView = peer.poseTransformation.map(RigidTransformationView(_, frame)) match {
    case Some(sv) => sv
    case None => throw new Exception("There is no rigid (pose) transformation associated with this ShapeModelTransformationView.")
  }

  def hasShapeTransformation(): Boolean = peer.gaussianProcessTransformation.isDefined
  def hasPoseTransformation(): Boolean = peer.poseTransformation.isDefined

}

object ShapeModelTransformationView {

  implicit object FindInSceneShapeModelTransformation extends FindInScene[ShapeModelTransformationView] {
    override def createView(s: SceneNode, frame: ScalismoFrame): Option[ShapeModelTransformationView] = {

      s match {
        case value: ShapeModelTransformationsNode => Some(ShapeModelTransformationView(value, frame))
        case _ => None
      }
    }
  }

  implicit object CallbackShapeModelTransformation extends HandleCallback[ShapeModelTransformationView] {

    override def registerOnAdd[R](g: Group, f: ShapeModelTransformationView => R, frame: ScalismoFrame): Unit = {
      g.peer.listenTo(g.peer.shapeModelTransformations)
      g.peer.reactions += {
        case ChildAdded(collection, newNode: TransformationNode[_]) => f(ShapeModelTransformationView(g.peer.shapeModelTransformations, frame))
      }
    }

    override def registerOnRemove[R](g: Group, f: ShapeModelTransformationView => R, frame: ScalismoFrame): Unit = {
      g.peer.listenTo(g.peer.shapeModelTransformations)
      g.peer.reactions += {
        case ChildRemoved(collection, removedNode: TransformationNode[_]) => f(ShapeModelTransformationView(g.peer.shapeModelTransformations, frame))
      }
    }
  }

}
