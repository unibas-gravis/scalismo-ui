package scalismo.ui.api

import scalismo.geometry.{ Point, _3D, Vector }
import scalismo.image.DiscreteScalarImage
import scalismo.mesh.TriangleMesh
import scalismo.statisticalmodel.StatisticalMeshModel
import scalismo.ui._
import spire.math.Numeric
import scala.annotation.implicitNotFound
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag
import scalismo.geometry._2D
import scalismo.image.DiscreteImageDomain3D
import scalismo.image.DiscreteImageDomain
import scalismo.geometry.Index

@implicitNotFound(msg = "Cannot show object of given type (no implicit defined for ${A})")
trait Show[-A] {
  def show(a: A, name: String)(implicit scene: Scene): Unit

}

object Show {
  implicit object ShowMesh extends Show[TriangleMesh] {
    override def show(m: TriangleMesh, name: String)(implicit scene: Scene) {
      StaticMesh.createFromPeer(m, None, Some(name))
    }
  }

  implicit object ShowStatisticalMeshModel extends Show[StatisticalMeshModel] {
    override def show(sm: StatisticalMeshModel, name: String)(implicit scene: Scene) {
      ShapeModel.createFromPeer(sm, 1, Some(name))
    }
  }

  implicit object ShowPointCloud extends Show[Iterable[Point[_3D]]] {
    override def show(pc: Iterable[Point[_3D]], name: String)(implicit scene: Scene) {
      StaticPointCloud.createFromPeer(pc.toIndexedSeq, None, Some(name))
    }
  }

  implicit def showImage[P: Numeric: ClassTag: TypeTag] = new Show[DiscreteScalarImage[_3D, P]] {
    def show(image: DiscreteScalarImage[_3D, P], name: String)(implicit scene: Scene) {
      StaticImage3D.createFromPeer(image, None, Some(name))
    }
  }

  implicit def show2DImage[P: Numeric: ClassTag: TypeTag] = new Show[DiscreteScalarImage[_2D, P]] {
    def show(image: DiscreteScalarImage[_2D, P], name: String)(implicit scene: Scene) {
      val oneSliceImageDomain = DiscreteImageDomain[_3D](
        Point(image.domain.origin(0), image.domain.origin(1), 0),
        Vector(image.domain.spacing(0), image.domain.spacing(1), 0),
        Index(image.domain.size(0), image.domain.size(1), 1)
      )

      val oneSliceImage = DiscreteScalarImage(oneSliceImageDomain, image.values.toArray)
      StaticImage3D.createFromPeer(oneSliceImage, None, Some(name))
    }
  }

}