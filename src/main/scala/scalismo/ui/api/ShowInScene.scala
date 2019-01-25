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
import scalismo.geometry.{ Landmark, Point, EuclideanVector, _3D }
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

  def showInScene(a: A, name: String, group: Group): View

}

trait LowPriorityImplicits {

  def apply[A](implicit a: ShowInScene[A]): ShowInScene[A] = a

  implicit def showInSceneScalarField[A: Scalar: ClassTag] =
    new ShowInScene[DiscreteScalarField[_3D, DiscreteDomain[_3D], A]] {

      override type View = ScalarFieldView

      override def showInScene(sf: DiscreteScalarField[_3D, DiscreteDomain[_3D], A], name: String, group: Group): ScalarFieldView = {
        ScalarFieldView(group.peer.scalarFields.add(sf, name))
      }
    }

  implicit object CreateGenericTransformation extends ShowInScene[Point[_3D] => Point[_3D]] {
    override type View = TransformationView

    override def showInScene(t: (Point[_3D]) => Point[_3D], name: String, group: Group): View = {
      TransformationView(group.peer.genericTransformations.add(t, name))
    }
  }

}

object ShowInScene extends LowPriorityImplicits {

  implicit object ShowInSceneMesh extends ShowInScene[TriangleMesh[_3D]] {
    override type View = TriangleMeshView

    override def showInScene(mesh: TriangleMesh[_3D], name: String, group: Group): TriangleMeshView = {
      val groupNode = group.peer
      TriangleMeshView(groupNode.triangleMeshes.add(mesh, name))
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

  implicit def ShowScalarField[S: Scalar: ClassTag] = new ShowInScene[ScalarMeshField[S]] {
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

  implicit def ShowImage[S: Scalar: ClassTag] = new ShowInScene[DiscreteScalarImage[_3D, S]] {
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

    override def showInScene(gp: DiscreteLowRankGaussianProcess[_3D, DiscreteDomain[_3D], EuclideanVector[_3D]],
      name: String,
      group: Group): View = {

      val gpNode = group.peer.genericTransformations.add(DiscreteLowRankGpPointTransformation(gp), name)
      DiscreteLowRankGPTransformationView(gpNode)
    }
  }

  implicit object CreateShapeModelTransformation extends ShowInScene[ShapeModelTransformation] {
    override type View = ShapeModelTransformationView

    override def showInScene(transform: ShapeModelTransformation, name: String, group: Group): View = {
      val t = for {
        pose <- group.peer.shapeModelTransformations.addPoseTransformation(transform.poseTransformation)
        shape <- group.peer.shapeModelTransformations.addGaussianProcessTransformation(transform.shapeTransformation)
      } yield ShapeModelTransformationView(group.peer.shapeModelTransformations)
      t.get
    }
  }

}