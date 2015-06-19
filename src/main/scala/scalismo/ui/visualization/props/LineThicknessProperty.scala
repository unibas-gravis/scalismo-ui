package scalismo.ui.visualization.props

import scalismo.ui.visualization.VisualizationProperty

class LineThicknessProperty(initial: Option[Int]) extends VisualizationProperty[Int, LineThicknessProperty] {
  override lazy val defaultValue = 1

  override def newInstance() = new LineThicknessProperty(None)

  override protected def sanitizeValue(newValue: Int): Int = Math.max(1, newValue)

  initial.foreach(c => value = c)

}

trait HasLineThickness {
  def lineThickness: LineThicknessProperty
}

