package scalismo.ui.model

import scalismo.geometry._3D
import scalismo.image.DiscreteScalarImage
import scalismo.ui.model.capabilities.{ Grouped, Removeable, Renameable, RenderableSceneNode }
import scalismo.ui.model.properties._

class ImagesNode(override val parent: GroupNode) extends SceneNodeCollection[ImageNode] {
  override val name: String = "Images"

  def add(image: DiscreteScalarImage[_3D, Float], name: String): ImageNode = {
    val node = new ImageNode(this, image, name)
    add(node)
    node
  }

}

class ImageNode(override val parent: ImagesNode, val source: DiscreteScalarImage[_3D, Float], initialName: String) extends RenderableSceneNode with Grouped with Renameable with Removeable with HasWindowLevel with HasOpacity {
  name = initialName

  val (minimumValue, maximumValue) = {
    // we manually do this instead of using the min or max methods of the iterator
    // so that we only have to run through the list once.
    var min: Float = Float.MaxValue
    var max: Float = Float.MinValue
    source.values.foreach { value =>
      min = Math.min(min, value)
      max = Math.max(max, value)
    }
    (min, max)
  }

  override val windowLevel: WindowLevelProperty = {
    val range = maximumValue - minimumValue
    val window = range / 4
    val level = minimumValue + range / 2
    new WindowLevelProperty(WindowLevel(window, level))
  }

  override val opacity: OpacityProperty = new OpacityProperty()

  override def group: GroupNode = parent.parent

  override def remove(): Unit = parent.remove(this)
}