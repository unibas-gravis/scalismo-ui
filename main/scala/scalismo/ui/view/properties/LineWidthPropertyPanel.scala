package scalismo.ui.view.properties

import javax.swing.border.TitledBorder

import scalismo.ui.model.SceneNode
import scalismo.ui.model.properties.{ HasLineWidth, LineWidthProperty, NodeProperty }
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.util.FancySlider

import scala.swing.BorderPanel
import scala.swing.event.ValueChanged

object LineWidthPropertyPanel extends PropertyPanel.Factory {
  override def create(frame: ScalismoFrame): PropertyPanel = {
    new LineWidthPropertyPanel(frame)
  }
}

class LineWidthPropertyPanel(override val frame: ScalismoFrame) extends BorderPanel with PropertyPanel {
  override def description: String = "2D Outline Width"

  private var targets: List[HasLineWidth] = Nil

  private val slider = new FancySlider {
    min = 1
    max = LineWidthProperty.MaxValue
    value = 1
  }

  layout(new BorderPanel {
    val sliderPanel = new BorderPanel {
      border = new TitledBorder(null, description, TitledBorder.LEADING, 0, null, null)
      layout(slider) = BorderPanel.Position.Center
    }
    layout(sliderPanel) = BorderPanel.Position.Center
  }) = BorderPanel.Position.North

  listenToOwnEvents()

  def listenToOwnEvents() = {
    listenTo(slider)
  }

  def deafToOwnEvents() = {
    deafTo(slider)
  }

  def updateUi() = {
    deafToOwnEvents()
    targets.headOption.foreach(t => slider.value = t.lineWidth.value)
    listenToOwnEvents()
  }

  override def setNodes(nodes: List[SceneNode]): Boolean = {
    cleanup()
    val supported = allOf[HasLineWidth](nodes)
    if (supported.nonEmpty) {
      targets = supported
      listenTo(targets.head.lineWidth)
      updateUi()
      true
    } else false
  }

  def cleanup(): Unit = {
    targets.headOption.foreach(t => deafTo(t.lineWidth))
    targets = Nil
  }

  reactions += {
    case NodeProperty.event.PropertyChanged(_) => updateUi()
    case ValueChanged(c) => targets.foreach(_.lineWidth.value = slider.value)
  }

}
