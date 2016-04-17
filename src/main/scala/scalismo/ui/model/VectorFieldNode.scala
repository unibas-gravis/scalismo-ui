package scalismo.ui.model

import scalismo.common.DiscreteVectorField
import scalismo.geometry.{Point3D, _3D}
import scalismo.ui.model.capabilities._
import scalismo.ui.model.properties._

class VectorFieldsNode(override val parent: GroupNode) extends SceneNodeCollection[VectorFieldNode] {
  override val name: String = "Scalar fields"

  def add(vectorField: DiscreteVectorField[_3D, _3D], name: String): VectorFieldNode = {
    val node = new VectorFieldNode(this, vectorField, name)
    add(node)
    node
  }
}

class VectorFieldNode(override val parent: VectorFieldsNode, val source: DiscreteVectorField[_3D, _3D], initialName: String)
  extends RenderableSceneNode with Removeable with Renameable with Grouped
    with HasOpacity with HasLineWidth with HasScalarRange
{

  name = initialName

  // we store the vectors as a sequence, as values are defined by iterators, which we cannot
  // traverse twice
  val vectors = source.values.toIndexedSeq

  override val opacity = new OpacityProperty()
  override val lineWidth = new LineWidthProperty()
  override val scalarRange: ScalarRangeProperty = {
    val (min, max) = {val norms = vectors.map(_.norm); (norms.min.toFloat, norms.max.toFloat) }
    new ScalarRangeProperty(ScalarRange(min, max, min, max))
  }

  override def group: GroupNode = parent.parent

  override def remove(): Unit = parent.remove(this)

}

