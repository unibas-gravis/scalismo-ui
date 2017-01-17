package scalismo.ui.api

import scalismo.common._
import scalismo.geometry.{ Landmark, Point, Vector, _3D }
import scalismo.image.DiscreteScalarImage
import scalismo.mesh.{ LineMesh, ScalarMeshField, TriangleMesh }
import scalismo.registration.{ RigidTransformation, RigidTransformationSpace }
import scalismo.statisticalmodel.{ DiscreteLowRankGaussianProcess, LowRankGaussianProcess, StatisticalMeshModel }
import scalismo.ui.model._
import scalismo.ui.view.ScalismoFrame

import scala.annotation.implicitNotFound
import scala.reflect.ClassTag
import scala.util.Try

@implicitNotFound(msg = "Don't know how to handle object (no implicit defined for ${A})")
trait ShowInScene[-A] {
  type View

  def showInScene(a: A, name: String, group: Group, frame: ScalismoFrame): View

}

trait LowPriorityImplicits {

  def apply[A](implicit a: ShowInScene[A]): ShowInScene[A] = a

  implicit def showInSceneScalarField[A: Scalar: ClassTag] = new ShowInScene[DiscreteScalarField[_3D, A]] {
    override type View = ScalarFieldView

    override def showInScene(sf: DiscreteScalarField[_3D, A], name: String, group: Group, frame: ScalismoFrame): ScalarFieldView = {
      ScalarFieldView(group.peer.scalarFields.add(sf, name), frame)
    }
  }

  implicit object CreateGenericTransformation extends ShowInScene[Point[_3D] => Point[_3D]] {
    override type View = TransformationView

    override def showInScene(t: (Point[_3D]) => Point[_3D], name: String, group: Group, frame: ScalismoFrame): View = {
      TransformationView(group.peer.genericTransformations.add(t, name), frame)
    }
  }

}

object ShowInScene extends LowPriorityImplicits {

  implicit object ShowInSceneMesh$ extends ShowInScene[TriangleMesh[_3D]] {
    override type View = TriangleMeshView

    override def showInScene(mesh: TriangleMesh[_3D], name: String, group: Group, frame: ScalismoFrame): TriangleMeshView = {
      val groupNode = group.peer
      TriangleMeshView(groupNode.triangleMeshes.add(mesh, name), frame)
    }

  }

  implicit object ShowInSceneLineMesh extends ShowInScene[LineMesh[_3D]] {
    override type View = LineMeshView

    override def showInScene(mesh: LineMesh[_3D], name: String, group: Group, frame: ScalismoFrame): LineMeshView = {
      val groupNode = group.peer
      LineMeshView(groupNode.lineMeshes.add(mesh, name), frame)
    }

  }

  implicit object ShowInScenePointCloudFromIndexedSeq extends ShowInScene[IndexedSeq[Point[_3D]]] {
    override type View = PointCloudView

    override def showInScene(pointCloud: IndexedSeq[Point[_3D]], name: String, group: Group, frame: ScalismoFrame): PointCloudView = {
      val groupNode = group.peer
      PointCloudView(groupNode.pointClouds.add(pointCloud, name), frame)
    }

  }

  implicit object ShowInScenePointCloudFromDomain extends ShowInScene[UnstructuredPointsDomain[_3D]] {
    override type View = PointCloudView

    override def showInScene(domain: UnstructuredPointsDomain[_3D], name: String, group: Group, frame: ScalismoFrame): PointCloudView = {
      ShowInScenePointCloudFromIndexedSeq.showInScene(domain.pointSequence, name, group, frame)
    }
  }

  implicit def ShowScalarField[S: Scalar: ClassTag] = new ShowInScene[ScalarMeshField[S]] {
    override type View = ScalarMeshFieldView

    override def showInScene(scalarMeshField: ScalarMeshField[S], name: String, group: Group, frame: ScalismoFrame): ScalarMeshFieldView = {
      val scalarConv = implicitly[Scalar[S]]
      val smfAsFloat = scalarMeshField.copy(data = scalarMeshField.data.map[Float](x => scalarConv.toFloat(x)))
      ScalarMeshFieldView(group.peer.scalarMeshFields.add(smfAsFloat, name), frame)
    }
  }

  implicit object ShowInSceneDiscreteFieldOfVectors extends ShowInScene[DiscreteField[_3D, Vector[_3D]]] {
    override type View = VectorFieldView

    override def showInScene(df: DiscreteField[_3D, Vector[_3D]], name: String, group: Group, frame: ScalismoFrame): VectorFieldView = {
      val discreteVectorField = DiscreteVectorField(df.domain, df.values.toIndexedSeq)
      VectorFieldView(group.peer.vectorFields.add(discreteVectorField, name), frame)
    }
  }

  implicit def ShowImage[S: Scalar: ClassTag] = new ShowInScene[DiscreteScalarImage[_3D, S]] {
    override type View = ImageView

    override def showInScene(image: DiscreteScalarImage[_3D, S], name: String, group: Group, frame: ScalismoFrame): ImageView = {
      val scalarConv = implicitly[Scalar[S]]
      val floatImage = image.map(x => scalarConv.toFloat(x))
      ImageView(group.peer.images.add(floatImage, name), frame)
    }
  }

  implicit object ShowInSceneLandmark$ extends ShowInScene[Landmark[_3D]] {
    override type View = LandmarkView

    override def showInScene(lm: Landmark[_3D], name: String, group: Group, frame: ScalismoFrame): LandmarkView = {
      LandmarkView(group.peer.landmarks.add(lm), frame)
    }

  }

  implicit object ShowInSceneLandmarks extends ShowInScene[Seq[Landmark[_3D]]] {
    override type View = Seq[LandmarkView]

    override def showInScene(lms: Seq[Landmark[_3D]], name: String, group: Group, frame: ScalismoFrame): Seq[LandmarkView] = {
      val landmarkViews = for (lm <- lms) yield {
        LandmarkView(group.peer.landmarks.add(lm), frame)
      }
      landmarkViews
    }

  }

  implicit object ShowInSceneVectorField extends ShowInScene[DiscreteVectorField[_3D, _3D]] {
    override type View = VectorFieldView

    override def showInScene(sf: DiscreteVectorField[_3D, _3D], name: String, group: Group, frame: ScalismoFrame): VectorFieldView = {
      VectorFieldView(group.peer.vectorFields.add(sf, name), frame)
    }
  }

  implicit object ShowInSceneStatisticalMeshModel$ extends ShowInScene[StatisticalMeshModel] {
    type View = StatisticalMeshModelViewControls

    override def showInScene(model: StatisticalMeshModel, name: String, group: Group, frame: ScalismoFrame): View = {

      val shapeModelTransform = ShapeModelTransformation(PointTransformation.RigidIdentity, DiscreteLowRankGpPointTransformation(model.gp))
      val smV = CreateShapeModelTransformation.showInScene(shapeModelTransform, "Statistical Mesh Model", group, frame)
      val tmV = ShowInSceneMesh$.showInScene(model.referenceMesh, name, group, frame)
      StatisticalMeshModelViewControls(tmV, smV)

    }
  }

  implicit object ShowInSceneTransformationGlypth extends ShowInScene[TransformationGlyph] {
    override type View = VectorFieldView

    override def showInScene(transformationGlyph: TransformationGlyph, name: String, group: Group, frame: ScalismoFrame): ShowInSceneTransformationGlypth.View = {
      VectorFieldView(group.peer.vectorFields.addTransformationGlyph(transformationGlyph.points.toIndexedSeq, name), frame)
    }
  }

  implicit object CreateRigidTransformation extends ShowInScene[RigidTransformation[_3D]] {
    override type View = RigidTransformationView

    override def showInScene(t: RigidTransformation[_3D], name: String, group: Group, frame: ScalismoFrame): View = {
      RigidTransformationView(group.peer.genericTransformations.add(t, name), frame)
    }
  }

  implicit object CreateLowRankGPTransformation extends ShowInScene[LowRankGaussianProcess[_3D, Vector[_3D]]] {
    override type View = LowRankGPTransformationView

    override def showInScene(gp: LowRankGaussianProcess[_3D, Vector[_3D]], name: String, group: Group, frame: ScalismoFrame): View = {
      val gpNode = group.peer.genericTransformations.add(LowRankGpPointTransformation(gp), name)
      LowRankGPTransformationView(gpNode, frame)
    }
  }

  implicit object CreateDiscreteLowRankGPTransformation extends ShowInScene[DiscreteLowRankGaussianProcess[_3D, Vector[_3D]]] {
    override type View = DiscreteLowRankGPTransformationView

    override def showInScene(gp: DiscreteLowRankGaussianProcess[_3D, Vector[_3D]], name: String, group: Group, frame: ScalismoFrame): View = {
      val gpNode = group.peer.genericTransformations.add(DiscreteLowRankGpPointTransformation(gp), name)
      DiscreteLowRankGPTransformationView(gpNode, frame)
    }
  }

  implicit object CreateShapeModelTransformation extends ShowInScene[ShapeModelTransformation] {
    override type View = ShapeModelTransformationView

    override def showInScene(transform: ShapeModelTransformation, name: String, group: Group, frame: ScalismoFrame): View = {
      val t = for {
        pose <- group.peer.shapeModelTransformations.addPoseTransformation(transform.poseTransformation, "model-pose")
        shape <- group.peer.shapeModelTransformations.addGaussianProcessTransformation(transform.shapeTransformation, "model-shape")
      } yield ShapeModelTransformationView(group.peer.shapeModelTransformations, frame)
      t.get
    }
  }

}