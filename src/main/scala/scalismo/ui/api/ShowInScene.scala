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

import scalismo.common._
import scalismo.geometry.{EuclideanVector, Landmark, Point, _3D}
import scalismo.image.DiscreteScalarImage
import scalismo.mesh.{LineMesh, ScalarMeshField, TriangleMesh, VertexColorMesh3D}
import scalismo.registration.RigidTransformation
import scalismo.statisticalmodel.{DiscreteLowRankGaussianProcess, LowRankGaussianProcess, StatisticalMeshModel, StatisticalVolumeMeshModel}
import scalismo.tetramesh.{ScalarVolumeMeshField, TetrahedralMesh, TetrahedralMesh3D}
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

  implicit def showInSceneScalarField[A: Scalar: ClassTag]: ShowInScene[DiscreteScalarField[_3D, DiscreteDomain[_3D], A]] {
    type View = ScalarFieldView
  } =
    new ShowInScene[DiscreteScalarField[_3D, DiscreteDomain[_3D], A]] {

      override type View = ScalarFieldView

      override def showInScene(sf: DiscreteScalarField[_3D, DiscreteDomain[_3D], A], name: String, group: Group): ScalarFieldView = {
        ScalarFieldView(group.peer.scalarFields.add(sf, name))
      }
    }

  implicit object CreateGenericTransformation extends ShowInScene[Point[_3D] => Point[_3D]] {
    override type View = TransformationView

    override def showInScene(t: Point[_3D] => Point[_3D], name: String, group: Group): View = {
      TransformationView(group.peer.genericTransformations.add(t, name))
    }
  }

  implicit object ShowInSceneMesh extends ShowInScene[TriangleMesh[_3D]] {
    override type View = TriangleMeshView

    override def showInScene(mesh: TriangleMesh[_3D], name: String, group: Group): TriangleMeshView = {
      val groupNode = group.peer
      TriangleMeshView(groupNode.triangleMeshes.add(mesh, name))
    }

  }

}

object ShowInScene extends LowPriorityImplicits {

  implicit def ShowVertexColorMesh: ShowInScene[VertexColorMesh3D] {
    type View = VertexColorMeshView
  } = new ShowInScene[VertexColorMesh3D] {
    override type View = VertexColorMeshView

    override def showInScene(mesh: VertexColorMesh3D, name: String, group: Group): VertexColorMeshView = {

      VertexColorMeshView(group.peer.colorMeshes.add(mesh, name))
    }
  }

  implicit def ShowTetrahedralMesh: ShowInScene[TetrahedralMesh[_3D]] {
    type View = TetrahedralMeshView
  } = new ShowInScene[TetrahedralMesh[_3D]] {
    override type View = TetrahedralMeshView

    override def showInScene(mesh: TetrahedralMesh[_3D], name: String, group: Group): TetrahedralMeshView = {
      TetrahedralMeshView(group.peer.tetrahedralMeshes.add(mesh, name))
    }
  }

  implicit def ShowTetrahedralMeshScalarField[S: Scalar: ClassTag]: ShowInScene[ScalarVolumeMeshField[S]] {
    type View = ScalarTetrahedralMeshFieldView
  } = new ShowInScene[ScalarVolumeMeshField[S]] {
    override type View = ScalarTetrahedralMeshFieldView

    override def showInScene(mesh: ScalarVolumeMeshField[S], name: String, group: Group): ScalarTetrahedralMeshFieldView = {
      val scalarConv = implicitly[Scalar[S]]
      val smfAsFloat = mesh.copy(data = mesh.data.map[Float](x => scalarConv.toFloat(x)))
      ScalarTetrahedralMeshFieldView(group.peer.tetrahedralMeshFields.add(smfAsFloat, name))
    }
  }

  implicit object ShowInSceneLineMesh extends ShowInScene[LineMesh[_3D]] {
    override type View = LineMeshView

    override def showInScene(mesh: LineMesh[_3D], name: String, group: Group): LineMeshView = {
      val groupNode = group.peer
      LineMeshView(groupNode.lineMeshes.add(mesh, name))
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

  implicit def ShowScalarField[S: Scalar: ClassTag]: ShowInScene[ScalarMeshField[S]] {
    type View = ScalarMeshFieldView
  } = new ShowInScene[ScalarMeshField[S]] {
    override type View = ScalarMeshFieldView

    override def showInScene(scalarMeshField: ScalarMeshField[S], name: String, group: Group): ScalarMeshFieldView = {
      val scalarConv = implicitly[Scalar[S]]
      val smfAsFloat = scalarMeshField.copy(data = scalarMeshField.data.map[Float](x => scalarConv.toFloat(x)))
      ScalarMeshFieldView(group.peer.scalarMeshFields.add(smfAsFloat, name))
    }
  }

  implicit object ShowInSceneDiscreteFieldOfVectors
      extends ShowInScene[DiscreteField[_3D, DiscreteDomain[_3D], EuclideanVector[_3D]]] {

    override type View = VectorFieldView

    override def showInScene(df: DiscreteField[_3D, DiscreteDomain[_3D], EuclideanVector[_3D]], name: String, group: Group): VectorFieldView = {
      VectorFieldView(group.peer.vectorFields.add(df, name))
    }
  }

  implicit def ShowImage[S: Scalar: ClassTag]: ShowInScene[DiscreteScalarImage[_3D, S]] {
    type View = ImageView
  } = new ShowInScene[DiscreteScalarImage[_3D, S]] {
    override type View = ImageView

    override def showInScene(image: DiscreteScalarImage[_3D, S], name: String, group: Group): ImageView = {
      val scalarConv = implicitly[Scalar[S]]
      val floatImage = image.map(x => scalarConv.toFloat(x))
      ImageView(group.peer.images.add(floatImage, name))
    }
  }

  implicit object ShowInSceneLandmark extends ShowInScene[Landmark[_3D]] {
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

  implicit object ShowInSceneStatisticalMeshModel extends ShowInScene[StatisticalMeshModel] {
    type View = StatisticalMeshModelViewControls

    override def showInScene(model: StatisticalMeshModel, name: String, group: Group): View = {

      val shapeModelTransform = ShapeModelTransformation(PointTransformation.RigidIdentity, DiscreteLowRankGpPointTransformation(model.gp))
      val smV = CreateShapeModelTransformation.showInScene(shapeModelTransform, name, group)
      val tmV = ShowInSceneMesh.showInScene(model.referenceMesh, name, group)
      StatisticalMeshModelViewControls(tmV, smV)

    }
  }

  implicit object ShowInSceneStatisticalVolumeMeshModel extends ShowInScene[StatisticalVolumeMeshModel] {
    type View = StatisticalVolumeMeshModelViewControls

    override def showInScene(model: StatisticalVolumeMeshModel, name: String, group: Group): View = {

      val shapeModelTransform = ShapeModelTransformation(PointTransformation.RigidIdentity, DiscreteLowRankGpPointTransformation(model.gp))
      val smV = CreateShapeModelTransformation.showInScene(shapeModelTransform, name, group)
      val tmV = ShowTetrahedralMesh.showInScene(model.referenceMeshVolume, name, group)
      StatisticalVolumeMeshModelViewControls(tmV, smV)

    }
  }

  implicit object ShowInSceneTransformationGlyph extends ShowInScene[TransformationGlyph] {
    override type View = VectorFieldView

    override def showInScene(transformationGlyph: TransformationGlyph, name: String, group: Group): ShowInSceneTransformationGlyph.View = {
      VectorFieldView(group.peer.vectorFields.addTransformationGlyph(transformationGlyph.points.toIndexedSeq, name))
    }
  }

  implicit object CreateRigidTransformation extends ShowInScene[RigidTransformation[_3D]] {
    override type View = RigidTransformationView

    override def showInScene(t: RigidTransformation[_3D], name: String, group: Group): View = {
      RigidTransformationView(group.peer.genericTransformations.add(t, name))
    }
  }

  implicit object CreateLowRankGPTransformation
      extends ShowInScene[LowRankGaussianProcess[_3D, EuclideanVector[_3D]]] {

    override type View = LowRankGPTransformationView

    override def showInScene(gp: LowRankGaussianProcess[_3D, EuclideanVector[_3D]], name: String, group: Group): View = {
      val gpNode = group.peer.genericTransformations.add(LowRankGpPointTransformation(gp), name)
      LowRankGPTransformationView(gpNode)
    }
  }

  implicit object CreateDiscreteLowRankGPTransformation
      extends ShowInScene[DiscreteLowRankGaussianProcess[_3D, DiscreteDomain[_3D], EuclideanVector[_3D]]] {

    override type View = DiscreteLowRankGPTransformationView

    override def showInScene(
      gp: DiscreteLowRankGaussianProcess[_3D, DiscreteDomain[_3D], EuclideanVector[_3D]],
      name: String,
      group: Group
    ): View = {

      val gpNode = group.peer.genericTransformations.add(DiscreteLowRankGpPointTransformation(gp), name)
      DiscreteLowRankGPTransformationView(gpNode)
    }
  }

  implicit object CreateShapeModelTransformation extends ShowInScene[ShapeModelTransformation] {
    override type View = ShapeModelTransformationView

    override def showInScene(transform: ShapeModelTransformation, name: String, group: Group): View = {
      val t = for {
        _ <- group.peer.shapeModelTransformations.addPoseTransformation(transform.poseTransformation)
        _ <- group.peer.shapeModelTransformations.addGaussianProcessTransformation(transform.shapeTransformation)
      } yield ShapeModelTransformationView(group.peer.shapeModelTransformations)
      t.get
    }
  }

}