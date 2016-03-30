package scalismo.ui.view.properties

import javax.swing.border.TitledBorder

import scalismo.ui.model.SceneNode
import scalismo.ui.model.properties.{ HasOpacity, NodeProperty }
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.util.FancySlider

import scala.swing.BorderPanel
import scala.swing.event.ValueChanged

object OpacityPropertyPanel extends PropertyPanel.Factory {

  override def create(frame: ScalismoFrame): PropertyPanel = {
    new OpacityPropertyPanel(frame)
  }
}

class OpacityPropertyPanel(override val frame: ScalismoFrame) extends BorderPanel with PropertyPanel {
  override def description: String = "Opacity"

  private var targets: List[HasOpacity] = Nil

  private val slider = new FancySlider {
    min = 0
    max = 100
    value = 100

    override def formattedValue(sliderValue: Int): String = s"$sliderValue%"
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
    targets.headOption.foreach(t => slider.value = (t.opacity.value * 100.0f).toInt)
    listenToOwnEvents()
  }

  override def setNodes(nodes: List[SceneNode]): Boolean = {
    cleanup()
    val supported = allMatch[HasOpacity](nodes)
    if (supported.nonEmpty) {
      targets = supported
      listenTo(targets.head.opacity)
      updateUi()
      true
    } else false
  }

  def cleanup(): Unit = {
    targets.headOption.foreach(t => deafTo(t.opacity))
    targets = Nil
  }

  reactions += {
    case NodeProperty.event.PropertyChanged(_) => updateUi()
    case ValueChanged(c) => targets.foreach(_.opacity.value = slider.value.toFloat / 100.0f)
  }

}
