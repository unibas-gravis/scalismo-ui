package scalismo.ui.model.properties

object RadiusProperty {
  val DefaultValue: Float = 5.0f

}

class RadiusProperty(initialValue: Float) extends NodeProperty[Float](initialValue) {
  def this() = this(RadiusProperty.DefaultValue)

  override protected def sanitize(possiblyNotSane: Float): Float = {
    Math.max(0, possiblyNotSane)
  }
}

trait HasRadius {
  def radius: RadiusProperty
}
