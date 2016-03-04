package scalismo.ui.model

import scalismo.ui.model.capabilities.Renameable
import scalismo.ui.model.properties.{ ColorProperty, HasColor, HasOpacity, OpacityProperty }

class PointCloudsNode(override val parent: GroupNode) extends SceneNodeCollection[PointCloudNode] {
  override val name: String = "Point Clouds"

  def add(pointCloud: PointCloud, name: String): PointCloudNode = {
    val node = new PointCloudNode(this, pointCloud, name)
    add(node)
    node
  }
}

class PointCloudNode(override val parent: PointCloudsNode, val pointCloud: PointCloud, initialName: => String) extends SceneNode with Renameable with HasColor with HasOpacity {
  name = initialName

  override val color = new ColorProperty()
  override val opacity = new OpacityProperty()
}

