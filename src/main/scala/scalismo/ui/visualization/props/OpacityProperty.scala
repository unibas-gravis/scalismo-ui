package scalismo.ui.visualization.props

import scalismo.ui.visualization.VisualizationProperty

class OpacityProperty(initial: Option[Float]) extends VisualizationProperty[Float, OpacityProperty] {
  override lazy val defaultValue = 1.0f

  override def newInstance() = new OpacityProperty(None)

  override protected def sanitizeValue(newValue: Float): Float = Math.max(0.0f, Math.min(1.0f, newValue))

  initial.foreach(i => value = i)
}

trait HasOpacity {
  def opacity: OpacityProperty
}

