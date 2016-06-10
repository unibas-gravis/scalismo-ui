package scalismo.ui.model

import scalismo.common.{UnstructuredPointsDomain, DiscreteVectorField}
import scalismo.geometry.{Point3D, Vector3D, _3D}
import scalismo.ui.model.capabilities._
import scalismo.ui.model.properties._


class TransformationGlyphNode(override val parent: VectorFieldsNode, val points: PointCloud, initialName: String)
  extends VectorFieldNode(parent, DiscreteVectorField(UnstructuredPointsDomain(points), points.map(_ => Vector3D(0,0,0))), initialName) with Transformable[DiscreteVectorField[_3D, _3D]] with InverseTransformation {

  lazy val glyphPoints =  points.toIndexedSeq

  override def transform(untransformed: DiscreteVectorField[_3D, _3D], transformation: PointTransformation): DiscreteVectorField[_3D, _3D] = {
    DiscreteVectorField(untransformed.domain,glyphPoints.map(p => transformation(p) - p))
  }


  override def scalarRange: ScalarRangeProperty = {
    val (min, max) = {
      val norms = transformedSource.values.toIndexedSeq.map(_.norm)
      (norms.min.toFloat, norms.max.toFloat)
    }
    new ScalarRangeProperty(ScalarRange(min, max, min, max))
  }


  override def inverseTransform(point: Point3D): Point3D = ???
}