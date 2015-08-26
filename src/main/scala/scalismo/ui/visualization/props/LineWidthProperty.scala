package scalismo.ui.visualization.props

import scalismo.ui.visualization.VisualizationProperty

class LineWidthProperty(initial: Option[Int]) extends VisualizationProperty[Int, LineWidthProperty] {
  override lazy val defaultValue = 1

  override def newInstance() = new LineWidthProperty(None)

  override protected def sanitizeValue(newValue: Int): Int = Math.max(1, newValue)

  initial.foreach(c => value = c)

}

trait HasLineWidth {
  def lineWidth: LineWidthProperty
}

