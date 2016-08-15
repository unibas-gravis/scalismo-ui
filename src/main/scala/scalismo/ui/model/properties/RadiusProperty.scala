package scalismo.ui.model.properties

object RadiusProperty {
  val DefaultValue: Double = 5.0

}

class RadiusProperty(initialValue: Double) extends NodeProperty[Double](initialValue) {
  def this() = this(RadiusProperty.DefaultValue)

  override protected def sanitize(possiblyNotSane: Double): Double = {
    Math.max(0, possiblyNotSane)
  }
}

trait HasRadius {
  def radius: RadiusProperty
}
