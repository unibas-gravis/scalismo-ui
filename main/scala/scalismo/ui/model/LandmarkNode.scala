package scalismo.ui.model

import java.awt.Color

import scalismo.geometry.{ Landmark, _3D }
import scalismo.ui.model.capabilities.{ Removeable, Renameable, Transformable }
import scalismo.ui.model.properties._

class LandmarksNode(override val parent: GroupNode) extends SceneNodeCollection[LandmarkNode] {
  override val name: String = "Landmarks"

  def add(landmark: Landmark[_3D], name: String): LandmarkNode = {
    val node = new LandmarkNode(this, landmark, name)
    add(node)
    node
  }
}

class LandmarkNode(override val parent: LandmarksNode, override val source: Landmark[_3D], initialName: String) extends Transformable[Landmark[_3D]] with Removeable with Renameable with HasColor with HasOpacity with HasLineWidth {
  name = initialName

  override val color = new ColorProperty(Color.BLUE)
  override val opacity = new OpacityProperty()
  override val lineWidth = new LineWidthProperty()

  override def remove(): Unit = parent.remove(this)

  override def transform(untransformed: Landmark[_3D], transformation: PointTransformation): Landmark[_3D] = {
    untransformed.copy(point = transformation(untransformed.point))
  }

  override def transformationsNode: TransformationsNode = parent.parent.transformations
}

