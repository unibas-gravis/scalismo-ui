package scalismo.ui.model.properties

object OpacityProperty {
  val DefaultValue: Double = 1.0
}

class OpacityProperty(initialValue: Double) extends NodeProperty[Double](initialValue) {
  def this() = this(OpacityProperty.DefaultValue)

  override protected def sanitize(possiblyNotSane: Double): Double = {
    Math.max(0.0, Math.min(1.0, possiblyNotSane))
  }
}

trait HasOpacity {
  def opacity: OpacityProperty
}
