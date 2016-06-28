package scalismo.ui.api

import scalismo.common._
import scalismo.geometry.{ Landmark, Point, _3D }
import scalismo.image.DiscreteScalarImage
import scalismo.mesh.{ ScalarMeshField, TriangleMesh }
import scalismo.registration.{ RigidTransformation, RigidTransformationSpace }
import scalismo.statisticalmodel.{ DiscreteLowRankGaussianProcess, LowRankGaussianProcess, StatisticalMeshModel }
import scalismo.ui.model._

import scala.annotation.implicitNotFound
import scala.reflect.ClassTag

@implicitNotFound(msg = "Don't know how to handle object (no implicit defined for ${A})")
trait ShowInScene[-A] {
  type View

  def showInScene(a: A, name: String, group: Group): View

}

trait LowPriorityImplicits {

  def apply[A](implicit a: ShowInScene[A]): ShowInScene[A] = a

  implicit def showInSceneScalarField[A: Scalar: ClassTag] = new ShowInScene[DiscreteScalarField[_3D, A]] {
    override type View = ScalarFieldView

    override def showInScene(sf: DiscreteScalarField[_3D, A], name: String, group: Group): ScalarFieldView = {
      ScalarFieldView(group.peer.scalarFields.add(sf, name))
    }
  }

  implicit object CreateGenericTransformation extends ShowInScene[Point[_3D] => Point[_3D]] {
    override type View = TransformationView

    override def showInScene(t: (Point[_3D]) => Point[_3D], name: String, group: Group): View = {
      TransformationView(group.peer.transformations.add(t, name))
    }
  }

}

object ShowInScene extends LowPriorityImplicits {

  implicit object ShowInSceneMesh$ extends ShowInScene[TriangleMesh[_3D]] {
    override type View = TriangleMeshView

    override def showInScene(mesh: TriangleMesh[_3D], name: String, group: Group): TriangleMeshView = {
      val groupNode = group.peer
      TriangleMeshView(groupNode.triangleMeshes.add(mesh, name))
    }

  }

  implicit object ShowInScenePointCloudFromIndexedSeq extends ShowInScene[IndexedSeq[Point[_3D]]] {
    override type View = PointCloudView

    override def showInScene(pointCloud: IndexedSeq[Point[_3D]], name: String, group: Group): PointCloudView = {
      val groupNode = group.peer
      PointCloudView(groupNode.pointClouds.add(pointCloud, name))
    }

  }

  implicit object ShowInScenePointCloudFromDomain extends ShowInScene[UnstructuredPointsDomain[_3D]] {
    override type View = PointCloudView

    override def showInScene(domain: UnstructuredPointsDomain[_3D], name: String, group: Group): PointCloudView = {
      ShowInScenePointCloudFromIndexedSeq.showInScene(domain.pointSequence, name, group)
    }
  }

  implicit def ShowScalarField[S: Scalar: ClassTag] = new ShowInScene[ScalarMeshField[S]] {
    override type View = ScalarMeshFieldView

    override def showInScene(scalarMeshField: ScalarMeshField[S], name: String, group: Group): ScalarMeshFieldView = {
      val scalarConv = implicitly[Scalar[S]]
      val smfAsFloat = scalarMeshField.copy(data = scalarMeshField.data.map[Float](x => scalarConv.toFloat(x)))
      ScalarMeshFieldView(group.peer.scalarMeshFields.add(smfAsFloat, name))
    }
  }

  implicit def ShowImage[S: Scalar: ClassTag] = new ShowInScene[DiscreteScalarImage[_3D, S]] {
    override type View = ImageView

    override def showInScene(image: DiscreteScalarImage[_3D, S], name: String, group: Group): ImageView = {
      val scalarConv = implicitly[Scalar[S]]
      val floatImage = image.map(x => scalarConv.toFloat(x))
      ImageView(group.peer.images.add(floatImage, name))
    }
  }

  implicit object ShowInSceneLandmark$ extends ShowInScene[Landmark[_3D]] {
    override type View = LandmarkView

    override def showInScene(lm: Landmark[_3D], name: String, group: Group): LandmarkView = {
      LandmarkView(group.peer.landmarks.add(lm))
    }

  }

  implicit object ShowInSceneLandmarks extends ShowInScene[Seq[Landmark[_3D]]] {
    override type View = Seq[LandmarkView]

    override def showInScene(lms: Seq[Landmark[_3D]], name: String, group: Group): Seq[LandmarkView] = {
      val landmarkViews = for (lm <- lms) yield {
        LandmarkView(group.peer.landmarks.add(lm))
      }
      landmarkViews
    }

  }

  implicit object ShowInSceneVectorField extends ShowInScene[DiscreteVectorField[_3D, _3D]] {
    override type View = VectorFieldView

    override def showInScene(sf: DiscreteVectorField[_3D, _3D], name: String, group: Group): VectorFieldView = {
      VectorFieldView(group.peer.vectorFields.add(sf, name))
    }
  }

  implicit object ShowInSceneStatisticalMeshModel$ extends ShowInScene[StatisticalMeshModel] {
    type View = StatisticalMeshModelViewControls

    override def showInScene(model: StatisticalMeshModel, name: String, group: Group): StatisticalMeshModelViewControls = {
      val groupNode = group.peer
      val tmnode = groupNode.triangleMeshes.add(model.referenceMesh, name)
      val transNode = groupNode.transformations.add(DiscreteLowRankGpPointTransformation(model.gp), s"$name-shape")
      val r = RigidTransformationSpace[_3D]().transformForParameters(RigidTransformationSpace[_3D]().identityTransformParameters)
      val poseTrans = groupNode.transformations.add(r, s"$name-pose")
      StatisticalMeshModelViewControls(tmnode, transNode, poseTrans)
    }
  }

  implicit object ShowInSceneTransformationGlypth extends ShowInScene[TransformationGlyph] {
    override type View = VectorFieldView

    override def showInScene(transformationGlyph: TransformationGlyph, name: String, group: Group): ShowInSceneTransformationGlypth.View = {
      VectorFieldView(group.peer.vectorFields.addTransformationGlyph(transformationGlyph.points.toIndexedSeq, name))
    }
  }

  implicit object CreateRigidTransformation extends ShowInScene[RigidTransformation[_3D]] {
    override type View = RigidTransformationView

    override def showInScene(t: RigidTransformation[_3D], name: String, group: Group): View = {
      RigidTransformationView(group.peer.transformations.add(t, name))
    }
  }

  implicit object CreateLowRankGPTransformation extends ShowInScene[LowRankGaussianProcess[_3D, _3D]] {
    override type View = LowRankGPTransformationView

    override def showInScene(gp: LowRankGaussianProcess[_3D, _3D], name: String, group: Group): View = {
      val gpNode = group.peer.transformations.add(LowRankGpPointTransformation(gp), name)
      LowRankGPTransformationView(gpNode)
    }
  }

  implicit object CreateDiscreteLowRankGPTransformation extends ShowInScene[DiscreteLowRankGaussianProcess[_3D, _3D]] {
    override type View = DiscreteLowRankGPTransformationView

    override def showInScene(gp: DiscreteLowRankGaussianProcess[_3D, _3D], name: String, group: Group): View = {
      val gpNode = group.peer.transformations.add(DiscreteLowRankGpPointTransformation(gp), name)
      DiscreteLowRankGPTransformationView(gpNode)
    }
  }

}