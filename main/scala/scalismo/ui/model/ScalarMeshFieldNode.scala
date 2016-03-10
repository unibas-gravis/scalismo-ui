package scalismo.ui.model

import scalismo.mesh.ScalarMeshField
import scalismo.ui.model.capabilities.{ Renameable, RenderableSceneNode }
import scalismo.ui.model.properties._

class ScalarMeshFieldsNode(override val parent: GroupNode) extends SceneNodeCollection[ScalarMeshFieldNode] {
  override val name: String = "Scalar Mesh Fields"

  def add(scalarMeshField: ScalarMeshField[Float], name: String): ScalarMeshFieldNode = {
    val node = new ScalarMeshFieldNode(this, scalarMeshField, name)
    add(node)
    node
  }
}

class ScalarMeshFieldNode(override val parent: ScalarMeshFieldsNode, val source: ScalarMeshField[Float], initialName: String) extends RenderableSceneNode with Renameable with HasOpacity with HasLineWidth with HasScalarRange {
  name = initialName

  override val opacity = new OpacityProperty()
  override val lineWidth = new LineWidthProperty()
  override val scalarRange: ScalarRangeProperty = {
    val (min, max) = (source.values.min, source.values.max)
    new ScalarRangeProperty(ScalarRange(min, max, min, max))
  }
}

