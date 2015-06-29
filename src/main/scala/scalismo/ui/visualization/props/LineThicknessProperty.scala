package scalismo.ui.visualization.props

import scalismo.ui.visualization.VisualizationProperty

class LineThicknessProperty(initial: Option[Int]) extends VisualizationProperty[Int, LineThicknessProperty] {
  def newInstance() = new LineThicknessProperty(None)

  lazy val defaultValue = 1
  initial.map(c => value = c)
}

trait HasLineThickness {
  def lineThickness: LineThicknessProperty
}

