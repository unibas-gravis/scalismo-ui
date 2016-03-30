package scalismo.ui.model.properties

object LineWidthProperty {
  val DefaultValue: Int = 1

  // this is a var so it can be changed if needed, but that is not recommended.
  var MaxValue: Int = 7
}

class LineWidthProperty(initialValue: Int) extends NodeProperty[Int](initialValue) {
  def this() = this(LineWidthProperty.DefaultValue)

  override protected def sanitize(possiblyNotSane: Int): Int = {
    Math.max(1, Math.min(LineWidthProperty.MaxValue, possiblyNotSane))
  }
}

trait HasLineWidth {
  def lineWidth: LineWidthProperty
}
