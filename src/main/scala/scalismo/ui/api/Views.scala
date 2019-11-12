/*
 * Copyright (C) 2016  University of Basel, Graphics and Vision Research Group 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scalismo.ui.api

import java.awt.Color

import breeze.linalg.DenseVector
import scalismo.common.{DiscreteDomain, DiscreteField, DiscreteScalarField}
import scalismo.geometry.{EuclideanVector, Landmark, Point, _3D}
import scalismo.image.DiscreteScalarImage
import scalismo.mesh.{LineMesh, ScalarMeshField, TriangleMesh, VertexColorMesh3D}
import scalismo.registration.RigidTransformation
import scalismo.statisticalmodel.DiscreteLowRankGaussianProcess
import scalismo.tetramesh.{ScalarVolumeMeshField, TetrahedralMesh3D}
import scalismo.ui.model.SceneNode.event.{ChildAdded, ChildRemoved}
import scalismo.ui.model._
import scalismo.ui.model.capabilities.Removeable
import scalismo.ui.model.properties.ScalarRange

sealed trait ObjectView {
  type PeerType <: SceneNode with Removeable

  protected[api] def peer: PeerType

  def name: String = peer.name

  def inGroup: Group = {
    Group(findBelongingGroup(peer))
  }

  def remove(): Unit = peer.remove()

  private def findBelongingGroup(node: SceneNode): GroupNode = {
    node match {
      case groupNode: GroupNode => groupNode
      case _ => findBelongingGroup(node.parent)
    }
  }
}

object ObjectView {

  implicit object FindInSceneObjectView extends FindInScene[ObjectView] {
    override def createView(s: SceneNode): Option[ObjectView] = {

      s match {
        case _: GroupNode => None // we ignore all group nodes, as they are not real objects
        case node: SceneNode with Removeable =>
          val ov = new ObjectView {
            override type PeerType = SceneNode with Removeable

            override protected[api] def peer: SceneNode with Removeable = node
          }
          Some(ov)
        case _ => None
      }
    }
  }

}

case class PointCloudView private[ui] (override protected[api] val peer: PointCloudNode) extends ObjectView {

  type PeerType = PointCloudNode

  def color: Color = peer.color.value

  def color_=(c: Color): Unit = {
    peer.color.value = c
  }

  def radius: Double = peer.radius.value

  def radius_=(r: Double): Unit = {
    peer.radius.value = r
  }

  def opacity: Double = peer.opacity.value

  def opacity_=(o: Double): Unit = {
    peer.opacity.value = o
  }

  def points: IndexedSeq[Point[_3D]] = peer.source

  def transformedPoints: IndexedSeq[Point[_3D]] = peer.transformedSource
}

object PointCloudView {

  implicit object FindInScenePointCloud extends FindInScene[PointCloudView] {
    override def createView(s: SceneNode): Option[PointCloudView] = {
      s match {
        case node: PointCloudNode => Some(PointCloudView(node))
        case _ => None
      }
    }
  }

  implicit def callbackPointCloudView: HandleCallback[PointCloudView] = new HandleCallback[PointCloudView] {

    override def registerOnAdd[R](g: Group, f: PointCloudView => R): Unit = {
      g.peer.listenTo(g.peer.pointClouds)
      g.peer.reactions += {
        case ChildAdded(_, newNode: PointCloudNode) =>
          val tmv = PointCloudView(newNode)
          f(tmv)
      }
    }

    override def registerOnRemove[R](g: Group, f: PointCloudView => R): Unit = {
      g.peer.listenTo(g.peer.pointClouds)
      g.peer.reactions += {
        case ChildRemoved(_, removedNode: PointCloudNode) =>
          val tmv = PointCloudView(removedNode)
          f(tmv)
      }
    }
  }
}

case class TriangleMeshView private[ui] (override protected[api] val peer: TriangleMeshNode) extends ObjectView {
  type PeerType = TriangleMeshNode

  def color: Color = peer.color.value

  def color_=(c: Color): Unit = {
    peer.color.value = c
  }

  def opacity: Double = peer.opacity.value

  def opacity_=(o: Double): Unit = {
    peer.opacity.value = o
  }

  def lineWidth: Int = peer.lineWidth.value

  def lineWidth_=(width: Int): Unit = {
    peer.lineWidth.value = width
  }

  def triangleMesh: TriangleMesh[_3D] = peer.source

  def transformedTriangleMesh: TriangleMesh[_3D] = peer.transformedSource
}

object TriangleMeshView {

  implicit object FindInSceneTriangleMeshView$ extends FindInScene[TriangleMeshView] {
    override def createView(s: SceneNode): Option[TriangleMeshView] = {
      s match {
        case peer: TriangleMeshNode => Some(TriangleMeshView(peer))
        case _ => None
      }
    }
  }

  implicit object callbacksTriangleMeshView extends HandleCallback[TriangleMeshView] {

    override def registerOnAdd[R](g: Group, f: TriangleMeshView => R): Unit = {
      g.peer.listenTo(g.peer.triangleMeshes)
      g.peer.reactions += {
        case ChildAdded(_, newNode: TriangleMeshNode) =>
          val tmv = TriangleMeshView(newNode)
          f(tmv)
      }
    }

    override def registerOnRemove[R](g: Group, f: TriangleMeshView => R): Unit = {
      g.peer.listenTo(g.peer.triangleMeshes)
      g.peer.reactions += {
        case ChildRemoved(_, removedNode: TriangleMeshNode) =>
          val tmv = TriangleMeshView(removedNode)
          f(tmv)
      }
    }

  }

}

case class VertexColorMeshView private[ui] (override protected[api] val peer: VertexColorMeshNode) extends ObjectView {

  type PeerType = VertexColorMeshNode

  def opacity: Double = peer.opacity.value

  def opacity_=(o: Double): Unit = {
    peer.opacity.value = o
  }

  def lineWidth: Int = peer.lineWidth.value

  def lineWidth_=(width: Int): Unit = {
    peer.lineWidth.value = width
  }

  def colorMesh: VertexColorMesh3D = peer.source

  def transformedTriangleMesh: VertexColorMesh3D = peer.transformedSource
}

object VertexColorMeshView {

  implicit object FindInSceneColorMesh extends FindInScene[VertexColorMeshView] {
    override def createView(s: SceneNode): Option[VertexColorMeshView] = {
      s match {
        case peer: VertexColorMeshNode => Some(VertexColorMeshView(peer))
        case _ => None
      }
    }
  }

  implicit object callbacksColorMeshView extends HandleCallback[VertexColorMeshView] {

    override def registerOnAdd[R](g: Group, f: VertexColorMeshView => R): Unit = {
      g.peer.listenTo(g.peer.colorMeshes)
      g.peer.reactions += {
        case ChildAdded(_, newNode: VertexColorMeshNode) =>
          val tmv = VertexColorMeshView(newNode)
          f(tmv)
      }
    }

    override def registerOnRemove[R](g: Group, f: VertexColorMeshView => R): Unit = {
      g.peer.listenTo(g.peer.colorMeshes)
      g.peer.reactions += {
        case ChildRemoved(_, removedNode: VertexColorMeshNode) =>
          val tmv = VertexColorMeshView(removedNode)
          f(tmv)
      }
    }
  }

}

case class TetrahedralMeshView private[ui] (override protected[api] val peer: TetrahedralMeshNode) extends ObjectView {

  type PeerType = TetrahedralMeshNode

  def opacity: Double = peer.opacity.value

  def opacity_=(o: Double): Unit = {
    peer.opacity.value = o
  }

  def lineWidth: Int = peer.lineWidth.value

  def lineWidth_=(width: Int): Unit = {
    peer.lineWidth.value = width
  }

  def tetrahedralMesh: TetrahedralMesh3D = peer.source

  def transformedTetrahedralMesh: TetrahedralMesh3D = peer.transformedSource
}

object TetrahedralMeshView {

  implicit object FindInSceneTetrahedralMeshView$ extends FindInScene[TetrahedralMeshView] {
    override def createView(s: SceneNode): Option[TetrahedralMeshView] = {
      s match {
        case peer: TetrahedralMeshNode => Some(TetrahedralMeshView(peer))
        case _ => None
      }
    }
  }

  implicit object callbacksTetrahedralMeshView extends HandleCallback[TetrahedralMeshView] {

    override def registerOnAdd[R](g: Group, f: TetrahedralMeshView => R): Unit = {
      g.peer.listenTo(g.peer.triangleMeshes)
      g.peer.reactions += {
        case ChildAdded(_, newNode: TetrahedralMeshNode) =>
          val tmv = TetrahedralMeshView(newNode)
          f(tmv)
      }
    }

    override def registerOnRemove[R](g: Group, f: TetrahedralMeshView => R): Unit = {
      g.peer.listenTo(g.peer.triangleMeshes)
      g.peer.reactions += {
        case ChildRemoved(_, removedNode: TetrahedralMeshNode) =>
          val tmv = TetrahedralMeshView(removedNode)
          f(tmv)
      }
    }
  }

}


case class ScalarTetrahedralMeshFieldView private[ui] (override protected[api] val peer: ScalarTetrahedralMeshFieldNode) extends ObjectView {

  type PeerType = ScalarTetrahedralMeshFieldNode

  def opacity: Double = peer.opacity.value

  def opacity_=(o: Double): Unit = {
    peer.opacity.value = o
  }

  def lineWidth: Int = peer.lineWidth.value

  def lineWidth_=(width: Int): Unit = {
    peer.lineWidth.value = width
  }

  def tetrahedralMeshField: ScalarVolumeMeshField[Float] = peer.source

  def transformedTetrahedralMeshField: ScalarVolumeMeshField[Float] = peer.transformedSource
}

object ScalarTetrahedralMeshFieldView {

  implicit object FindInSceneScalarTetrahedralMeshFieldView$ extends FindInScene[ScalarTetrahedralMeshFieldView] {
    override def createView(s: SceneNode): Option[ScalarTetrahedralMeshFieldView] = {
      s match {
        case peer: ScalarTetrahedralMeshFieldNode => Some(ScalarTetrahedralMeshFieldView(peer))
        case _ => None
      }
    }
  }

  implicit object callbacksScalarTetrahedralMeshFieldView extends HandleCallback[ScalarTetrahedralMeshFieldView] {

    override def registerOnAdd[R](g: Group, f: ScalarTetrahedralMeshFieldView => R): Unit = {
      g.peer.listenTo(g.peer.triangleMeshes)
      g.peer.reactions += {
        case ChildAdded(_, newNode: ScalarTetrahedralMeshFieldNode) =>
          val tmv = ScalarTetrahedralMeshFieldView(newNode)
          f(tmv)
      }
    }

    override def registerOnRemove[R](g: Group, f: ScalarTetrahedralMeshFieldView => R): Unit = {
      g.peer.listenTo(g.peer.triangleMeshes)
      g.peer.reactions += {
        case ChildRemoved(_, removedNode: ScalarTetrahedralMeshFieldNode) =>
          val tmv = ScalarTetrahedralMeshFieldView(removedNode)
          f(tmv)
      }
    }
  }

}


case class LineMeshView private[ui] (override protected[api] val peer: LineMeshNode) extends ObjectView {

  type PeerType = LineMeshNode

  def color: Color = peer.color.value

  def color_=(c: Color): Unit = {
    peer.color.value = c
  }

  def opacity: Double = peer.opacity.value

  def opacity_=(o: Float): Unit = {
    peer.opacity.value = o
  }

  def lineWidth: Int = peer.lineWidth.value

  def lineWidth_=(width: Int): Unit = {
    peer.lineWidth.value = width
  }

  def lineMesh: LineMesh[_3D] = peer.source

  def transformedLineMesh: LineMesh[_3D] = peer.transformedSource
}

object LineMeshView {

  implicit object FindInSceneLineMeshView extends FindInScene[LineMeshView] {
    override def createView(s: SceneNode): Option[LineMeshView] = {
      s match {
        case peer: LineMeshNode => Some(LineMeshView(peer))
        case _ => None
      }
    }
  }

  implicit object callbackLineMeshView extends HandleCallback[LineMeshView] {

    override def registerOnAdd[R](g: Group, f: LineMeshView => R): Unit = {
      g.peer.listenTo(g.peer.lineMeshes)
      g.peer.reactions += {
        case ChildAdded(_, newNode: LineMeshNode) =>
          val lmv = LineMeshView(newNode)
          f(lmv)
      }
    }

    override def registerOnRemove[R](g: Group, f: LineMeshView => R): Unit = {
      g.peer.listenTo(g.peer.lineMeshes)
      g.peer.reactions += {
        case ChildRemoved(_, removedNode: LineMeshNode) =>
          val lmv = LineMeshView(removedNode)
          f(lmv)
      }
    }

  }

}

case class LandmarkView private[ui] (override protected[api] val peer: LandmarkNode) extends ObjectView {

  type PeerType = LandmarkNode

  def color: Color = peer.color.value

  def color_=(c: Color): Unit = {
    peer.color.value = c
  }

  def opacity: Double = peer.opacity.value

  def opacity_=(o: Double): Unit = {
    peer.opacity.value = o
  }

  def landmark: Landmark[_3D] = peer.source

  def transformedLandmark: Landmark[_3D] = peer.transformedSource
}

object LandmarkView {

  implicit object FindInSceneLandmarkView$ extends FindInScene[LandmarkView] {
    override def createView(s: SceneNode): Option[LandmarkView] = {
      s match {
        case peer: LandmarkNode => Some(LandmarkView(peer))
        case _ => None
      }
    }
  }

  implicit object CallbackLandmarkView extends HandleCallback[LandmarkView] {

    override def registerOnAdd[R](g: Group, f: LandmarkView => R): Unit = {
      g.peer.listenTo(g.peer.landmarks)
      g.peer.reactions += {
        case ChildAdded(_, newNode: LandmarkNode) =>
          val tmv = LandmarkView(newNode)
          f(tmv)
      }
    }

    override def registerOnRemove[R](g: Group, f: LandmarkView => R): Unit = {
      g.peer.listenTo(g.peer.landmarks)
      g.peer.reactions += {
        case ChildRemoved(_, removedNode: LandmarkNode) =>
          val tmv = LandmarkView(removedNode)
          f(tmv)
      }
    }
  }

}

case class ScalarMeshFieldView private[ui] (override protected[api] val peer: ScalarMeshFieldNode) extends ObjectView {
  type PeerType = ScalarMeshFieldNode

  def scalarRange: ScalarRange = peer.scalarRange.value

  def scalarRange_=(s: ScalarRange): Unit = {
    peer.scalarRange.value = s
  }

  def opacity: Double = peer.opacity.value

  def opacity_=(o: Double): Unit = {
    peer.opacity.value = o
  }

  def lineWidth: Int = peer.lineWidth.value

  def lineWidth_=(width: Int): Unit = {
    peer.lineWidth.value = width
  }

  def scalarMeshField: ScalarMeshField[Float] = peer.source

  def transformedScalarMeshField: ScalarMeshField[Float] = peer.transformedSource
}

object ScalarMeshFieldView {

  implicit object FindInSceneScalarMeshField extends FindInScene[ScalarMeshFieldView] {
    override def createView(s: SceneNode): Option[ScalarMeshFieldView] = {
      s match {
        case node: ScalarMeshFieldNode => Some(ScalarMeshFieldView(node))
        case _ => None
      }
    }
  }

  implicit object CallbackScalarMeshFieldView extends HandleCallback[ScalarMeshFieldView] {

    override def registerOnAdd[R](g: Group, f: ScalarMeshFieldView => R): Unit = {
      g.peer.listenTo(g.peer.scalarMeshFields)
      g.peer.reactions += {
        case ChildAdded(_, newNode: ScalarMeshFieldNode) =>
          val tmv = ScalarMeshFieldView(newNode)
          f(tmv)
      }
    }

    override def registerOnRemove[R](g: Group, f: ScalarMeshFieldView => R): Unit = {
      g.peer.listenTo(g.peer.scalarMeshFields)
      g.peer.reactions += {
        case ChildRemoved(_, removedNode: ScalarMeshFieldNode) =>
          val tmv = ScalarMeshFieldView(removedNode)
          f(tmv)
      }
    }
  }

}

case class ScalarFieldView private[ui] (override protected[api] val peer: ScalarFieldNode) extends ObjectView {
  type PeerType = ScalarFieldNode

  def scalarRange: ScalarRange = peer.scalarRange.value

  def scalarRange_=(s: ScalarRange): Unit = {
    peer.scalarRange.value = s
  }

  def radius: Double = peer.radius.value

  def radius_=(r: Double): Unit = {
    peer.radius.value = r
  }

  def opacity: Double = peer.opacity.value

  def opacity_=(o: Double): Unit = {
    peer.opacity.value = o
  }

  def scalarField: DiscreteScalarField[_3D, DiscreteDomain[_3D], Float] = peer.source

  def transformedScalarField: DiscreteScalarField[_3D, DiscreteDomain[_3D], Float] = peer.transformedSource
}

object ScalarFieldView {

  implicit object FindInSceneScalarMeshField extends FindInScene[ScalarFieldView] {
    override def createView(s: SceneNode): Option[ScalarFieldView] = {
      s match {
        case node: ScalarFieldNode => Some(ScalarFieldView(node))
        case _ => None
      }
    }
  }

  implicit object CallbackScalarFieldView extends HandleCallback[ScalarFieldView] {

    override def registerOnAdd[R](g: Group, f: ScalarFieldView => R): Unit = {
      g.peer.listenTo(g.peer.scalarFields)
      g.peer.reactions += {
        case ChildAdded(_, newNode: ScalarFieldNode) =>
          val tmv = ScalarFieldView(newNode)
          f(tmv)
      }
    }

    override def registerOnRemove[R](g: Group, f: ScalarFieldView => R): Unit = {
      g.peer.listenTo(g.peer.scalarFields)
      g.peer.reactions += {
        case ChildRemoved(_, removedNode: ScalarFieldNode) =>
          val tmv = ScalarFieldView(removedNode)
          f(tmv)
      }
    }
  }

}

case class VectorFieldView private[ui] (override protected[api] val peer: VectorFieldNode) extends ObjectView {
  type PeerType = VectorFieldNode

  def scalarRange: ScalarRange = peer.scalarRange.value

  def scalarRange_=(s: ScalarRange): Unit = {
    peer.scalarRange.value = s
  }

  def opacity: Double = peer.opacity.value

  def opacity_=(o: Double): Unit = {
    peer.opacity.value = o
  }

  def vectorField[A]: DiscreteField[_3D, DiscreteDomain[_3D], EuclideanVector[_3D]] = peer.source
}

object VectorFieldView {

  implicit object FindInSceneScalarMeshField extends FindInScene[VectorFieldView] {
    override def createView(s: SceneNode): Option[VectorFieldView] = {
      s match {
        case node: VectorFieldNode => Some(VectorFieldView(node))
        case _ => None
      }
    }
  }

  implicit object CallbackVectorFieldView extends HandleCallback[VectorFieldView] {

    override def registerOnAdd[R](g: Group, f: VectorFieldView => R): Unit = {
      g.peer.listenTo(g.peer.vectorFields)
      g.peer.reactions += {
        case ChildAdded(_, newNode: VectorFieldNode) =>
          val tmv = VectorFieldView(newNode)
          f(tmv)
      }
    }

    override def registerOnRemove[R](g: Group, f: VectorFieldView => R): Unit = {
      g.peer.listenTo(g.peer.vectorFields)
      g.peer.reactions += {
        case ChildRemoved(_, removedNode: VectorFieldNode) =>
          val tmv = VectorFieldView(removedNode)
          f(tmv)
      }
    }
  }

}

case class ImageView private[ui] (override protected[api] val peer: ImageNode) extends ObjectView {
  type PeerType = ImageNode

  def opacity: Double = peer.opacity.value

  def opacity_=(o: Double): Unit = {
    peer.opacity.value = o
  }

  def window: Double = peer.windowLevel.value.window

  def window_=(w: Double): Unit = {
    peer.windowLevel.value = peer.windowLevel.value.copy(window = w)
  }

  def level: Double = peer.windowLevel.value.level

  def level_=(w: Double): Unit = {
    peer.windowLevel.value = peer.windowLevel.value.copy(level = w)
  }

  def image: DiscreteScalarImage[_3D, Float] = peer.source
}

object ImageView {

  implicit object FindImage extends FindInScene[ImageView] {
    override def createView(s: SceneNode): Option[ImageView] = {
      s match {
        case imageNode: ImageNode => Some(ImageView(imageNode))
        case _ => None
      }
    }
  }

  implicit object CallbackLandmarkView extends HandleCallback[ImageView] {

    override def registerOnAdd[R](g: Group, f: ImageView => R): Unit = {
      g.peer.listenTo(g.peer.images)
      g.peer.reactions += {
        case ChildAdded(_, newNode: ImageNode) =>
          val imv = ImageView(newNode)
          f(imv)
      }
    }

    override def registerOnRemove[R](g: Group, f: ImageView => R): Unit = {
      g.peer.listenTo(g.peer.images)
      g.peer.reactions += {
        case ChildRemoved(_, removedNode: ImageNode) =>
          val tmv = ImageView(removedNode)
          f(tmv)
      }
    }
  }

}

// Note this class does not extend Object view, as there is not really a corresponding node to this concept
case class StatisticalMeshModelViewControls private[ui] (meshView: TriangleMeshView, shapeModelTransformationView: ShapeModelTransformationView)

case class Group(override protected[api] val peer: GroupNode) extends ObjectView {

  def hidden_=(b: Boolean): Unit = {
    peer.hidden = b
  }

  def hidden: Boolean = peer.hidden

  type PeerType = GroupNode
}

object Group {

  implicit object FindInSceneGroup$ extends FindInScene[Group] {
    override def createView(s: SceneNode): Option[Group] = {
      s match {
        case node: GroupNode => Some(Group(node))
        case _ => None
      }
    }
  }

}

case class TransformationView private[ui] (override protected[api] val peer: TransformationNode[Point[_3D] => Point[_3D]]) extends ObjectView {
  def transformation: Point[_3D] => Point[_3D] = peer.transformation

  def transformation_=(t: Point[_3D] => Point[_3D]): Unit = {
    peer.transformation = t
  }

  override type PeerType = TransformationNode[Point[_3D] => Point[_3D]]

}

object TransformationView {

  implicit object FindInSceneGenericTransformation$ extends FindInScene[TransformationView] {
    override def createView(s: SceneNode): Option[TransformationView] = {

      type PointToPointTransformation[D] = Point[D] => Point[D]
      // here we need a two step process due to type erasure to find the right type.
      s match {
        case value: TransformationNode[_] if value.transformation.isInstanceOf[PointToPointTransformation[_]] =>
          Some(TransformationView(s.asInstanceOf[TransformationNode[PointToPointTransformation[_3D]]]))
        case _ => None
      }
    }
  }

}

case class RigidTransformationView private[ui] (override protected[api] val peer: TransformationNode[RigidTransformation[_3D]]) extends ObjectView {

  override type PeerType = TransformationNode[RigidTransformation[_3D]]

  def transformation: RigidTransformation[_3D] = peer.transformation

  def transformation_=(transform: RigidTransformation[_3D]): Unit = {
    peer.transformation = transform
  }
}

object RigidTransformationView {

  implicit object FindInSceneRigidTransformation$ extends FindInScene[RigidTransformationView] {
    override def createView(s: SceneNode): Option[RigidTransformationView] = {

      // here we need a two step process due to type erasure to find the right type.
      s match {
        // filter out Rigid transformations that are part of a StatisticalShapeMoodelTransformation
        case value: ShapeModelTransformationComponentNode[_] if value.transformation.isInstanceOf[RigidTransformation[_]] => None
        case value: TransformationNode[_] if value.transformation.isInstanceOf[RigidTransformation[_]] =>
          Some(RigidTransformationView(s.asInstanceOf[TransformationNode[RigidTransformation[_3D]]]))
        case _ => None
      }
    }
  }

  implicit object CallbackRigidTransformation extends HandleCallback[RigidTransformationView] {

    override def registerOnAdd[R](g: Group, f: RigidTransformationView => R): Unit = {
      g.peer.listenTo(g.peer.genericTransformations)
      g.peer.reactions += {
        case ChildAdded(_, newNode: TransformationNode[_]) =>

          if (newNode.transformation.isInstanceOf[RigidTransformation[_]]) {
            val tmv = RigidTransformationView(newNode.asInstanceOf[TransformationNode[RigidTransformation[_3D]]])
            f(tmv)
          }

      }
    }

    override def registerOnRemove[R](g: Group, f: RigidTransformationView => R): Unit = {
      g.peer.listenTo(g.peer.genericTransformations)
      g.peer.reactions += {
        case ChildRemoved(_, removedNode: TransformationNode[_]) =>
          if (removedNode.transformation.isInstanceOf[RigidTransformation[_]]) {
            val tmv = RigidTransformationView(removedNode.asInstanceOf[TransformationNode[RigidTransformation[_3D]]])
            f(tmv)
          }
      }
    }
  }

}

case class DiscreteLowRankGPTransformationView private[ui] (override protected[api] val peer: TransformationNode[DiscreteLowRankGpPointTransformation]) extends ObjectView {

  override type PeerType = TransformationNode[DiscreteLowRankGpPointTransformation]

  def coefficients: DenseVector[Double] = peer.transformation.coefficients

  def coefficients_=(coefficients: DenseVector[Double]): Unit = {
    {
      peer.transformation = peer.transformation.copy(coefficients)
    }
  }

  val transformation: DiscreteLowRankGpPointTransformation = peer.transformation

  def discreteLowRankGaussianProcess: DiscreteLowRankGaussianProcess[_3D, DiscreteDomain[_3D], EuclideanVector[_3D]] = peer.transformation.dgp

  def discreteLowRankGaussianProcess_=(dgp: DiscreteLowRankGaussianProcess[_3D, DiscreteDomain[_3D], EuclideanVector[_3D]]): Unit = {
    peer.transformation = DiscreteLowRankGpPointTransformation(dgp)
  }
}

object DiscreteLowRankGPTransformationView {

  implicit object FindInSceneDiscreteGPTransformation$ extends FindInScene[DiscreteLowRankGPTransformationView] {
    override def createView(s: SceneNode): Option[DiscreteLowRankGPTransformationView] = {

      // here we need a two step process due to type erasure to find the right type.
      s match {
        // filter out Rigid transformations that are part of a StatisticalShapeMoodelTransformation
        case value: ShapeModelTransformationComponentNode[_] if value.transformation.isInstanceOf[DiscreteLowRankGpPointTransformation] => None
        case value: TransformationNode[_] if value.transformation.isInstanceOf[DiscreteLowRankGpPointTransformation] =>
          Some(DiscreteLowRankGPTransformationView(s.asInstanceOf[TransformationNode[DiscreteLowRankGpPointTransformation]]))
        case _ => None
      }
    }
  }

  implicit object CallbackDiscreteGPTransformation extends HandleCallback[DiscreteLowRankGPTransformationView] {

    override def registerOnAdd[R](g: Group, f: DiscreteLowRankGPTransformationView => R): Unit = {
      g.peer.listenTo(g.peer.genericTransformations)
      g.peer.reactions += {
        case ChildAdded(_, newNode: TransformationNode[_]) =>

          if (newNode.transformation.isInstanceOf[DiscreteLowRankGpPointTransformation]) {
            val tmv = DiscreteLowRankGPTransformationView(newNode.asInstanceOf[TransformationNode[DiscreteLowRankGpPointTransformation]])
            f(tmv)
          }

      }
    }

    override def registerOnRemove[R](g: Group, f: DiscreteLowRankGPTransformationView => R): Unit = {
      g.peer.listenTo(g.peer.genericTransformations)
      g.peer.reactions += {
        case ChildRemoved(_, removedNode: TransformationNode[_]) =>
          if (removedNode.transformation.isInstanceOf[DiscreteLowRankGpPointTransformation]) {
            val tmv = DiscreteLowRankGPTransformationView(removedNode.asInstanceOf[TransformationNode[DiscreteLowRankGpPointTransformation]])
            f(tmv)
          }
      }
    }
  }

}

case class LowRankGPTransformationView private[ui] (override protected[api] val peer: TransformationNode[LowRankGpPointTransformation]) extends ObjectView {

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
  def apply(poseTransformation: RigidTransformation[_3D], gp: DiscreteLowRankGaussianProcess[_3D, DiscreteDomain[_3D], EuclideanVector[_3D]]): ShapeModelTransformation = {
    ShapeModelTransformation(poseTransformation, DiscreteLowRankGpPointTransformation(gp))
  }
}

case class ShapeModelTransformationView private[ui] (override protected[api] val peer: ShapeModelTransformationsNode) extends ObjectView {

  override type PeerType = ShapeModelTransformationsNode

  def shapeTransformationView: DiscreteLowRankGPTransformationView = peer.gaussianProcessTransformation.map(DiscreteLowRankGPTransformationView(_)) match {
    case Some(sv) => sv
    case None => throw new Exception("There is no Gaussian Process (shape) transformation associated with this ShapeModelTransformationView.")
  }

  def poseTransformationView: RigidTransformationView = peer.poseTransformation.map(RigidTransformationView(_)) match {
    case Some(sv) => sv
    case None => throw new Exception("There is no rigid (pose) transformation associated with this ShapeModelTransformationView.")
  }

  def hasShapeTransformation: Boolean = peer.gaussianProcessTransformation.isDefined

  def hasPoseTransformation: Boolean = peer.poseTransformation.isDefined

}

object ShapeModelTransformationView {

  implicit object FindInSceneShapeModelTransformation extends FindInScene[ShapeModelTransformationView] {
    override def createView(s: SceneNode): Option[ShapeModelTransformationView] = {

      s match {
        case value: ShapeModelTransformationsNode => Some(ShapeModelTransformationView(value))
        case _ => None
      }
    }
  }

  implicit object CallbackShapeModelTransformation extends HandleCallback[ShapeModelTransformationView] {

    override def registerOnAdd[R](g: Group, f: ShapeModelTransformationView => R): Unit = {
      g.peer.listenTo(g.peer.shapeModelTransformations)
      g.peer.reactions += {
        case ChildAdded(_, _: TransformationNode[_]) => f(ShapeModelTransformationView(g.peer.shapeModelTransformations))
      }
    }

    override def registerOnRemove[R](g: Group, f: ShapeModelTransformationView => R): Unit = {
      g.peer.listenTo(g.peer.shapeModelTransformations)
      g.peer.reactions += {
        case ChildRemoved(_, _: TransformationNode[_]) => f(ShapeModelTransformationView(g.peer.shapeModelTransformations))
      }
    }
  }

}
