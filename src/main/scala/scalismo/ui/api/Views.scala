package scalismo.ui.api

import java.awt.Color

import breeze.linalg.DenseVector
import scalismo.geometry.{Landmark, Point, _3D}
import scalismo.image.DiscreteScalarImage
import scalismo.mesh.{ScalarMeshField, TriangleMesh}
import scalismo.registration.{RigidTransformationSpace, RigidTransformation}
import scalismo.statisticalmodel.{StatisticalMeshModel, DiscreteLowRankGaussianProcess, LowRankGaussianProcess}
import scalismo.ui.model.SceneNode.event.{ChildRemoved, ChildAdded}
import scalismo.ui.model._
import scalismo.ui.model.capabilities.Removeable

import scala.concurrent.Future




/**
  * Created by luetma00 on 08.04.16.
  */
trait ObjectView {
  type PeerType <: SceneNode with Removeable

  protected[api] def peer : PeerType

  def name : String = peer.name

  def inGroup : Group = {
    Group(findBelongingGroup(peer))
  }
  def remove() : Unit = peer.remove()



  private def findBelongingGroup(node : SceneNode) : GroupNode = {
    if (node.isInstanceOf[Group]) this.asInstanceOf[GroupNode]
    else findBelongingGroup(node.parent)
  }
}



case class TriangleMeshView(override val peer : TriangleMeshNode) extends ObjectView {
  type PeerType = TriangleMeshNode
  def color = peer.color.value
  def color_= (c : Color) : Unit = {peer.color.value = c}

  def triangleMesh : TriangleMesh = peer.source
}

object TriangleMeshView {

  implicit object FindInSceneTriangleMeshView$ extends FindInScene[TriangleMeshView] {
    override def createView(s: SceneNode): Option[TriangleMeshView] = {
      if (s.isInstanceOf[TriangleMeshNode]) Some(TriangleMeshView(s.asInstanceOf[TriangleMeshNode])) else None
    }
  }

  implicit def callbackTriangleMeshView = new HandleCallback[TriangleMeshView] {

    override def registerOnAdd[R](g : Group, f: TriangleMeshView => R) : Unit = {
      g.peer.listenTo(g.peer.triangleMeshes)
      g.peer.reactions += {
        case ChildAdded(collection, newNode)  => {
          val tmv = TriangleMeshView(newNode.asInstanceOf[TriangleMeshNode])
          f(tmv)
        }
      }
    }

    override def registerOnRemove[R](g : Group, f: TriangleMeshView => R) : Unit = {
      g.peer.listenTo(g.peer.triangleMeshes)
      g.peer.reactions += {
        case ChildRemoved(collection, removedNode)  => {
          val tmv = TriangleMeshView(removedNode.asInstanceOf[TriangleMeshNode])
          f(tmv)
        }
      }
    }

  }

}


case class LandmarkView(override val peer : LandmarkNode) extends ObjectView {
  type PeerType = LandmarkNode
  def color = peer.color.value
  def color_= (c : Color) : Unit = peer.color.value = c

  def landmark : Landmark[_3D] = peer.source
}

object LandmarkView {
  implicit object FindInSceneLandmarkView$ extends FindInScene[LandmarkView] {
    override def createView(s: SceneNode): Option[LandmarkView] = {
      if (s.isInstanceOf[LandmarkNode]) Some(LandmarkView(s.asInstanceOf[LandmarkNode])) else None
    }
  }


  implicit object CallbackLandmarkView extends HandleCallback[LandmarkView] {

    override def registerOnAdd[R](g: Group, f: LandmarkView => R): Unit = {
      g.peer.listenTo(g.peer.landmarks)
      g.peer.reactions += {
        case ChildAdded(collection, newNode) => {
          val tmv = LandmarkView(newNode.asInstanceOf[LandmarkNode])
          f(tmv)
        }
      }
    }

    override def registerOnRemove[R](g: Group, f: LandmarkView => R): Unit = {
      g.peer.listenTo(g.peer.landmarks)
      g.peer.reactions += {
        case ChildRemoved(collection, removedNode) => {
          val tmv = LandmarkView(removedNode.asInstanceOf[LandmarkNode])
          f(tmv)
        }
      }
    }
  }
}


case class ScalarMeshFieldView(override val peer : ScalarMeshFieldNode) extends ObjectView {
  type PeerType = ScalarMeshFieldNode

  def scalarMeshField : ScalarMeshField[Float] = peer.source
}


object ScalarMeshFieldView {
  implicit object FindInSceneScalarMeshFiled$ extends FindInScene[ScalarMeshFieldView] {
    override def createView(s: SceneNode): Option[ScalarMeshFieldView] = {
      if (s.isInstanceOf[ScalarMeshFieldNode]) Some(ScalarMeshFieldView(s.asInstanceOf[ScalarMeshFieldNode])) else None
    }
  }

}

case class ImageView(override val peer : ImageNode) extends ObjectView {
  type PeerType = ImageNode

  def image : DiscreteScalarImage[_3D, Float] = peer.source
}

object ImageView {
  implicit object FindImage extends FindInScene[ImageView] {
    override def createView(s: SceneNode): Option[ImageView] = {
      if (s.isInstanceOf[ImageView]) Some(ImageView(s.asInstanceOf[ImageNode])) else None
    }
  }

  implicit object CallbackLandmarkView extends HandleCallback[ImageView] {

    override def registerOnAdd[R](g: Group, f: ImageView => R): Unit = {
      g.peer.listenTo(g.peer.images)
      g.peer.reactions += {
        case ChildAdded(collection, newNode) => {
          val imv = ImageView(newNode.asInstanceOf[ImageNode])
          f(imv)
        }
      }
    }

    override def registerOnRemove[R](g: Group, f: ImageView => R): Unit = {
      g.peer.listenTo(g.peer.images)
      g.peer.reactions += {
        case ChildRemoved(collection, removedNode) => {
          val tmv = ImageView(removedNode.asInstanceOf[ImageNode])
          f(tmv)
        }
      }
    }
  }

}


// Note this class does not extend Object view, as there is not really a corresponding node to this concept
case class StatisticalMeshModelView(private val meshNode : TriangleMeshNode,
                                    private val shapeTransNode : TransformationNode[DiscreteLowRankGpPointTransformation],
                                    private val poseTransNode : TransformationNode[RigidTransformation[_3D]]) {

  def referenceMesh = TriangleMeshView(meshNode)
  def shapeTransformation = DiscreteLowRankGPTransformationView(shapeTransNode)
  def poseTransformation = RigidTransformationView(poseTransNode)

  // FIXME, do we need to take care of the transformation?
  def statisticalMeshModel : StatisticalMeshModel = StatisticalMeshModel(meshNode.source, shapeTransNode.transformation.dgp)
}




case class Group(override protected[api] val peer : GroupNode) extends ObjectView {

  type PeerType = GroupNode
}

object Group {
  implicit object FindInSceneGroup$ extends FindInScene[Group] {
    override def createView(s: SceneNode): Option[Group] = {
      if (s.isInstanceOf[GroupNode]) Some(Group(s.asInstanceOf[GroupNode])) else None
    }
  }

}


case class TransformationView(override val peer : TransformationNode[Point[_3D] => Point[_3D]]) extends ObjectView {
  def transformation : Point[_3D] => Point[_3D] = peer.transformation
  def transformation_=(t : Point[_3D] => Point[_3D]) : Unit = {peer.transformation= t}

  override type PeerType = TransformationNode[Point[_3D] => Point[_3D]]


}

object TransformationView {
  implicit object FindInSceneGenericTransformation$ extends FindInScene[TransformationView] {
    override def createView(s : SceneNode) : Option[TransformationView] = {

      type PointToPointTransformation = Point[_3D] => Point[_3D]
      // here we need a two step process due to type erasure to find the right type.
      if (s.isInstanceOf[TransformationNode[_]]
        && s.asInstanceOf[TransformationNode[_]].transformation.isInstanceOf[PointToPointTransformation]
      ) {
        Some(TransformationView(s.asInstanceOf[TransformationNode[PointToPointTransformation]]))
      }
      else None
    }
  }
}


case class RigidTransformationView(override val peer : TransformationNode[RigidTransformation[_3D]]) extends ObjectView  {

  override type PeerType = TransformationNode[RigidTransformation[_3D]]

  def transformation : RigidTransformation[_3D] = peer.transformation
  def transformation_=(transformation : RigidTransformation[_3D]) : Unit = {
    peer.transformation.copy(transformation)
  }
}

object RigidTransformationView {

  implicit object FindInSceneRigidTransformation$ extends FindInScene[RigidTransformationView] {
    override def createView(s : SceneNode) : Option[RigidTransformationView] = {

      // here we need a two step process due to type erasure to find the right type.
      if (s.isInstanceOf[TransformationNode[_]]
        && s.asInstanceOf[TransformationNode[_]].transformation.isInstanceOf[RigidTransformation[_]]
      ) {
        Some(RigidTransformationView(s.asInstanceOf[TransformationNode[RigidTransformation[_3D]]]))
      }
      else None
    }
  }

}

case class DiscreteLowRankGPTransformationView(override val peer : TransformationNode[DiscreteLowRankGpPointTransformation]) extends ObjectView {

  override type PeerType = TransformationNode[DiscreteLowRankGpPointTransformation]

  def coefficients : DenseVector[Float] = peer.transformation.coefficients
  def coefficients_=(coefficients : DenseVector[Float]) : Unit = {
    {peer.transformation = peer.transformation.copy(coefficients)}
  }
}

object DiscreteLowRankGPTransformationView {
  implicit object FindInSceneDiscreteGPTransformation$ extends FindInScene[DiscreteLowRankGPTransformationView] {
    override def createView(s : SceneNode) : Option[DiscreteLowRankGPTransformationView] = {

      // here we need a two step process due to type erasure to find the right type.
      if (s.isInstanceOf[TransformationNode[_]]
        && s.asInstanceOf[TransformationNode[_]].transformation.isInstanceOf[DiscreteLowRankGpPointTransformation]
      ) {
        Some(DiscreteLowRankGPTransformationView(s.asInstanceOf[TransformationNode[DiscreteLowRankGpPointTransformation]]))
      }
      else None
    }
  }

}

case class LowRankGPTransformationView(override val peer : TransformationNode[LowRankGpPointTransformation]) extends ObjectView {

  override type PeerType = TransformationNode[LowRankGpPointTransformation]

  def coefficients : DenseVector[Float] = peer.transformation.coefficients
  def coefficients_=(coefficients : DenseVector[Float]) : Unit = {
    {peer.transformation = peer.transformation.copy(coefficients)}
  }
}
