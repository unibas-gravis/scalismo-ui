package scalismo.ui.model

import scalismo.geometry.Point3D
import scalismo.mesh.ScalarMeshField
import scalismo.ui.model.capabilities._
import scalismo.ui.model.properties._

class ScalarMeshFieldsNode(override val parent: GroupNode) extends SceneNodeCollection[ScalarMeshFieldNode] {
  override val name: String = "Scalar Mesh Fields"

  def add(scalarMeshField: ScalarMeshField[Float], name: String): ScalarMeshFieldNode = {
    val node = new ScalarMeshFieldNode(this, scalarMeshField, name)
    add(node)
    node
  }
}

class ScalarMeshFieldNode(override val parent: ScalarMeshFieldsNode, override val source: ScalarMeshField[Float], initialName: String) extends Transformable[ScalarMeshField[Float]] with InverseTransformation with Removeable with Renameable with HasOpacity with HasLineWidth with HasScalarRange {
  name = initialName

  override val opacity = new OpacityProperty()
  override val lineWidth = new LineWidthProperty()
  override val scalarRange: ScalarRangeProperty = {
    val (min, max) = (source.values.min, source.values.max)
    new ScalarRangeProperty(ScalarRange(min, max, min, max))
  }

  override def group: GroupNode = parent.parent

  override def remove(): Unit = parent.remove(this)

  override def inverseTransform(point: Point3D): Point3D = {
    val id = transformedSource.mesh.findClosestPoint(point).id
    source.mesh.point(id)
  }

  override def transform(untransformed: ScalarMeshField[Float], transformation: PointTransformation): ScalarMeshField[Float] = {
    untransformed.copy(mesh = untransformed.mesh.transform(transformation))
  }

}

