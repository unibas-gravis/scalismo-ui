package scalismo.ui.api

import scalismo.common._
import scalismo.geometry.{ Index, Point, Vector, _2D, _3D }
import scalismo.image.{ DiscreteImageDomain, DiscreteScalarImage }
import scalismo.mesh.TriangleMesh
import scalismo.statisticalmodel.StatisticalMeshModel
import scalismo.statisticalmodel.asm.ASMSample
import scalismo.ui._

import scala.annotation.implicitNotFound
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

@implicitNotFound(msg = "Cannot show object of given type (no implicit defined for ${A})")
trait Show[-A] {
  def show(a: A, name: String)(implicit scene: Scene): Unit

}

object Show {
  implicit object ShowMesh extends Show[TriangleMesh] {
    override def show(m: TriangleMesh, name: String)(implicit scene: Scene): Unit = {
      StaticMesh.createFromPeer(m, None, Some(name))
    }
  }

  implicit object ShowStatisticalMeshModel extends Show[StatisticalMeshModel] {
    override def show(sm: StatisticalMeshModel, name: String)(implicit scene: Scene): Unit = {
      ShapeModel.createFromPeer(sm, 1, Some(name))
    }
  }

  implicit object ShowPointCloud extends Show[Iterable[Point[_3D]]] {
    override def show(pc: Iterable[Point[_3D]], name: String)(implicit scene: Scene): Unit = {
      StaticPointCloud.createFromPeer(pc.toIndexedSeq, None, Some(name))
    }
  }

  implicit object ShowVectorFieldCloud extends Show[DiscreteVectorField[_3D, _3D]] {
    override def show(vf: DiscreteVectorField[_3D, _3D], name: String)(implicit scene: Scene): Unit = {
      StaticVectorField.createFromPeer(vf, None, Some(name))
    }
  }

  implicit def show2DImage[P: Scalar: ClassTag: TypeTag] = new Show[DiscreteScalarImage[_2D, P]] {
    def show(image: DiscreteScalarImage[_2D, P], name: String)(implicit scene: Scene): Unit = {
      val oneSliceImageDomain = DiscreteImageDomain[_3D](
        Point(image.domain.origin(0), image.domain.origin(1), 0),
        Vector(image.domain.spacing(0), image.domain.spacing(1), 0),
        Index(image.domain.size(0), image.domain.size(1), 1))

      val oneSliceImage = DiscreteScalarImage(oneSliceImageDomain, ScalarArray(image.values.toArray))
      StaticImage3D.createFromPeer(oneSliceImage, None, Some(name))
    }
  }

  implicit def showScalarField[P: Scalar: ClassTag: TypeTag] = new Show[scalismo.common.DiscreteScalarField[_3D, P]] {

    override def show(scalarField: scalismo.common.DiscreteScalarField[_3D, P], name: String)(implicit scene: Scene): Unit = {
      scalarField match {
        case img: DiscreteScalarImage[_3D, P] => StaticImage3D.createFromPeer(img, None, Some(name))
        case meshField: scalismo.mesh.ScalarMeshField[P] => StaticScalarMeshField.createFromPeer(meshField, None, Some(name))
        case _ => StaticScalarField.createFromPeer(scalarField, None, Some(name))

      }
    }
  }

  implicit def showASMSample = new Show[ASMSample] {

    override def show(asmSample: ASMSample, name: String)(implicit scene: Scene): Unit = {

      val profilePointsAndVals = asmSample.featureField.pointsWithValues.toIndexedSeq
      val allPointsWithValues = profilePointsAndVals.map {
        case (p, vec) =>
          val profPoints = asmSample.featureExtractor.featurePoints(asmSample.mesh, asmSample.mesh.findClosestPoint(p)._2, p)
          profPoints.map { pts =>
            if (pts.size == vec.size) {
              pts.zip(vec.toArray)
            } else IndexedSeq()
          }.getOrElse(IndexedSeq())
      }

      val profileCloud = allPointsWithValues.flatten
      if (profileCloud.nonEmpty) {
        val field = new DiscreteScalarField[_3D, Float](UnstructuredPointsDomain(profileCloud.map(_._1)), ScalarArray[Float](profileCloud.map(_._2).toArray))
        StaticScalarField.createFromPeer(field, None, Some(name))
      }
    }
  }

}