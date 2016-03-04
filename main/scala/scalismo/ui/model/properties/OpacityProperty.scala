package scalismo.ui.model.properties

object OpacityProperty {
  val DefaultValue: Float = 1.0f
}

class OpacityProperty(initialValue: => Float) extends NodeProperty[Float](initialValue) {
  def this() = this(OpacityProperty.DefaultValue)

  override protected def sanitize(possiblyNotSane: Float): Float = {
    Math.max(0f, Math.min(1f, possiblyNotSane))
  }
}

trait HasOpacity {
  def opacity: OpacityProperty
}
