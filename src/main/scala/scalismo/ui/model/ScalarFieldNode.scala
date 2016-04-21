package scalismo.ui.model

import scalismo.common.{ DiscreteScalarField, Scalar }
import scalismo.geometry.{ Point3D, _3D }
import scalismo.ui.model.capabilities.{ InverseTransformation, Removeable, Renameable, Transformable }
import scalismo.ui.model.properties._

import scala.reflect.ClassTag

class ScalarFieldsNode(override val parent: GroupNode) extends SceneNodeCollection[ScalarFieldNode] {
  override val name: String = "Scalar fields"

  def add[S: Scalar: ClassTag](scalarField: DiscreteScalarField[_3D, S], name: String): ScalarFieldNode = {
    val scalar = implicitly[Scalar[S]]
    val node = new ScalarFieldNode(this, scalarField.map(s => scalar.toFloat(s)), name)
    add(node)
    node
  }
}

class ScalarFieldNode(override val parent: ScalarFieldsNode, override val source: DiscreteScalarField[_3D, Float], initialName: String)
    extends Transformable[DiscreteScalarField[_3D, Float]]
    with InverseTransformation with Removeable with Renameable
    with HasOpacity with HasRadius with HasLineWidth with HasScalarRange {

  name = initialName

  override val opacity = new OpacityProperty()
  override val radius = new RadiusProperty()
  override val lineWidth = new LineWidthProperty()
  override val scalarRange: ScalarRangeProperty = {
    val (min, max) = (source.values.min, source.values.max)
    new ScalarRangeProperty(ScalarRange(min, max, min, max))
  }

  override def group: GroupNode = parent.parent

  override def remove(): Unit = parent.remove(this)

  override def transform(untransformed: DiscreteScalarField[_3D, Float], transformation: PointTransformation): DiscreteScalarField[_3D, Float] = {
    val newDomain = untransformed.domain.transform(transformation)
    DiscreteScalarField(newDomain, untransformed.data)
  }

  override def inverseTransform(point: Point3D): Point3D = {
    val id = transformedSource.domain.findClosestPoint(point).id
    source.domain.point(id)
  }

}

