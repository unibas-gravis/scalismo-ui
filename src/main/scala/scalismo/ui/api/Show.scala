package scalismo.ui.api

import scalismo.common._
import scalismo.geometry._
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
      MeshView.createFromSource(m, None, Some(name))
    }
  }

  implicit object ShowStatisticalMeshModel extends Show[StatisticalMeshModel] {
    override def show(sm: StatisticalMeshModel, name: String)(implicit scene: Scene): Unit = {
      ShapeModelView.createFromSource(sm, 1, Some(name))
    }
  }

  implicit object ShowPointCloud extends Show[Iterable[Point[_3D]]] {
    override def show(pc: Iterable[Point[_3D]], name: String)(implicit scene: Scene): Unit = {
      PointCloudView.createFromSource(pc.toIndexedSeq, None, Some(name))
    }
  }

  implicit object ShowVectorFieldCloud extends Show[DiscreteVectorField[_3D, _3D]] {
    override def show(vf: DiscreteVectorField[_3D, _3D], name: String)(implicit scene: Scene): Unit = {
      VectorFieldView.createFromSource(vf, None, Some(name))
    }
  }

  implicit def show2DImage[P: Scalar: ClassTag: TypeTag] = new Show[DiscreteScalarImage[_2D, P]] {
    def show(image: DiscreteScalarImage[_2D, P], name: String)(implicit scene: Scene): Unit = {
      val oneSliceImageDomain = DiscreteImageDomain[_3D](
        Point(image.domain.origin(0), image.domain.origin(1), 0),
        Vector(image.domain.spacing(0), image.domain.spacing(1), 0),
        IntVector(image.domain.size(0), image.domain.size(1), 1))

      val oneSliceImage = DiscreteScalarImage(oneSliceImageDomain, ScalarArray(image.values.toArray))
      Image3DView.createFromSource(oneSliceImage, None, Some(name))
    }
  }

  implicit def showScalarField[P: Scalar: ClassTag: TypeTag] = new Show[scalismo.common.DiscreteScalarField[_3D, P]] {

    override def show(scalarField: scalismo.common.DiscreteScalarField[_3D, P], name: String)(implicit scene: Scene): Unit = {
      scalarField match {
        case img: DiscreteScalarImage[_3D, P] => Image3DView.createFromSource(img, None, Some(name))
        case meshField: scalismo.mesh.ScalarMeshField[P] => ScalarMeshFieldView.createFromSource(meshField, None, Some(name))
        case _ => ScalarFieldView.createFromSource(scalarField, None, Some(name))

      }
    }
  }

  implicit def showASMSample = new Show[ASMSample] {

    override def show(asmSample: ASMSample, name: String)(implicit scene: Scene): Unit = {

      val profilePointsAndVals = asmSample.featureField.pointsWithValues.toIndexedSeq
      val profileCloud = profilePointsAndVals.flatMap {
        case (p, vec) =>
          val profPoints = asmSample.featureExtractor.featurePoints(asmSample.mesh, asmSample.mesh.findClosestPoint(p).id, p)
          profPoints.map { pts =>
            if (pts.size == vec.size) {
              pts.zip(vec.toArray)
            } else IndexedSeq()
          }.getOrElse(IndexedSeq())
      }

      if (profileCloud.nonEmpty) {
        val field = new DiscreteScalarField[_3D, Float](UnstructuredPointsDomain(profileCloud.map(_._1)), ScalarArray[Float](profileCloud.map(_._2).toArray))
        ScalarFieldView.createFromSource(field, None, Some(name))
      }
    }
  }

}
