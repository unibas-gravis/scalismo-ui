package scalismo.ui.api

import scalismo.common.{DiscreteScalarField, Scalar}
import scalismo.geometry.{Point, Landmark, _3D}
import scalismo.mesh.{ScalarMeshField, TriangleMesh}
import scalismo.registration.{RigidTransformation, RigidTransformationSpace}
import scalismo.statisticalmodel.{DiscreteLowRankGaussianProcess, LowRankGaussianProcess, StatisticalMeshModel}
import scalismo.ui.model._

import scala.annotation.implicitNotFound
import scala.reflect.ClassTag

@implicitNotFound(msg = "Don't know how to handle object (no implicit defined for ${A})")
trait ShowInScene[-A] {
  type View

  def showInScene(a: A, name  : String, group : Group): View

}


object ShowInScene {

  def apply[A](implicit a : ShowInScene[A]) : ShowInScene[A] = a

  implicit object ShowInSceneMesh$ extends ShowInScene[TriangleMesh] {
    override type View = TriangleMeshView


    override def showInScene(mesh: TriangleMesh, name: String, group: Group): TriangleMeshView = {
      val groupNode = group.peer
      TriangleMeshView(groupNode.triangleMeshes.add(mesh, name))
    }

  }


  implicit def ShowScalarField[S : Scalar : ClassTag] = new ShowInScene[ScalarMeshField[S]] {
    override type View = ScalarMeshFieldView

    override def showInScene(scalarMeshField : ScalarMeshField[S], name : String, group : Group) : ScalarMeshFieldView = {
      val scalarConv = implicitly[Scalar[S]]
      val smfAsFloat = scalarMeshField.copy(data = scalarMeshField.data.map[Float](x => scalarConv.toFloat(x)))
      ScalarMeshFieldView(group.peer.scalarMeshFields.add(smfAsFloat, name))
    }
  }

  implicit object ShowInSceneLandmark$ extends ShowInScene[Landmark[_3D]] {
    override type View = LandmarkView


    override def showInScene(lm : Landmark[_3D], name: String, group: Group): LandmarkView = {
     LandmarkView(group.peer.landmarks.add(lm))
    }

  }

  implicit object ShowInSceneStatisticalMeshModel$ extends ShowInScene[StatisticalMeshModel] {
    type View = StatisticalMeshModelView

    override def showInScene(model: StatisticalMeshModel, name: String, group: Group): StatisticalMeshModelView = {
      val groupNode = group.peer
      val tmnode = groupNode.triangleMeshes.add(model.referenceMesh, name)
      val transNode = groupNode.transformations.add(DiscreteLowRankGpPointTransformation(model.gp), s"$name-shape")
      val r = RigidTransformationSpace[_3D]().transformForParameters(RigidTransformationSpace[_3D]().identityTransformParameters)
      val poseTrans = groupNode.transformations.add(r, s"$name-pose")
      StatisticalMeshModelView(tmnode, transNode, poseTrans)
    }
  }


  implicit object CreateGenericTransformation extends ShowInScene[Point[_3D] => Point[_3D]] {
    override type View = TransformationView

    override def showInScene(t: (Point[_3D]) => Point[_3D], name : String, group: Group): View = {
      TransformationView(group.peer.transformations.add(t, name))
    }
  }

  implicit object CreateRigidTransformation extends ShowInScene[RigidTransformation[_3D]] {
    override type View = RigidTransformationView

    override def showInScene(t: RigidTransformation[_3D], name : String, group: Group): View = {
      RigidTransformationView(group.peer.transformations.add(t, name))
    }
  }



  implicit object CreateLowRankGPTransformation extends ShowInScene[LowRankGaussianProcess[_3D, _3D]] {
    override type View = LowRankGPTransformationView

    override def showInScene(gp: LowRankGaussianProcess[_3D, _3D], name : String, group: Group): View = {
      val gpNode = group.peer.transformations.add(LowRankGpPointTransformation(gp), name)
      LowRankGPTransformationView(gpNode)
    }
  }


  implicit object CreateDiscreteLowRankGPTransformation extends ShowInScene[DiscreteLowRankGaussianProcess[_3D, _3D]] {
    override type View = DiscreteLowRankGPTransformationView

    override def showInScene(gp: DiscreteLowRankGaussianProcess[_3D, _3D], name : String, group: Group): View = {
      val gpNode = group.peer.transformations.add(DiscreteLowRankGpPointTransformation(gp), name)
      DiscreteLowRankGPTransformationView(gpNode)
    }
  }


}