package scalismo.ui.model.properties

object ScalarRangeProperty {
  val DefaultValue: ScalarRange = ScalarRange(0, 1, 0, 1)
}

class ScalarRangeProperty(initialValue: ScalarRange) extends NodeProperty[ScalarRange](initialValue) {
  def this() = this(ScalarRangeProperty.DefaultValue)


  override protected def sanitize(newValue: ScalarRange): ScalarRange = {
    if (newValue.absoluteMaximum < newValue.absoluteMinimum) {
      sanitize(newValue.copy(absoluteMaximum = newValue.absoluteMinimum))
    } else if (newValue.cappedMaximum > newValue.absoluteMaximum) {
      sanitize(newValue.copy(cappedMaximum = newValue.absoluteMaximum))
    } else if (newValue.cappedMinimum < newValue.absoluteMinimum) {
      sanitize(newValue.copy(cappedMinimum = newValue.absoluteMinimum))
    } else if (newValue.cappedMaximum < newValue.cappedMinimum) {
      sanitize(newValue.copy(cappedMaximum = newValue.cappedMinimum))
    } else newValue
  }
}

trait HasScalarRange {
  def scalarRange: ScalarRangeProperty
}
