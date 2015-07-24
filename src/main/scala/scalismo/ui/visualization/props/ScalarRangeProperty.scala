package scalismo.ui.visualization.props

import scalismo.ui.visualization.{ ScalarRange, VisualizationProperty }

class ScalarRangeProperty(initial: Option[ScalarRange]) extends VisualizationProperty[ScalarRange, ScalarRangeProperty] {
  override lazy val defaultValue = ScalarRange(0, 1, 0, 1)

  override def newInstance() = new ScalarRangeProperty(None)

  override protected def sanitizeValue(newValue: ScalarRange): ScalarRange = {
    if (newValue.absoluteMaximum < newValue.absoluteMinimum) {
      sanitizeValue(newValue.copy(absoluteMaximum = newValue.absoluteMinimum))
    } else if (newValue.cappedMaximum > newValue.absoluteMaximum) {
      sanitizeValue(newValue.copy(cappedMaximum = newValue.absoluteMaximum))
    } else if (newValue.cappedMinimum < newValue.absoluteMinimum) {
      sanitizeValue(newValue.copy(cappedMinimum = newValue.absoluteMinimum))
    } else if (newValue.cappedMaximum < newValue.cappedMinimum) {
      sanitizeValue(newValue.copy(cappedMaximum = newValue.cappedMinimum))
    } else newValue
  }

  initial.foreach(i => value = i)
}

trait HasScalarRange {
  def scalarRange: ScalarRangeProperty
}

