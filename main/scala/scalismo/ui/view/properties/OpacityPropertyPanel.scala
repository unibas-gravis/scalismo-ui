package scalismo.ui.view.properties

import javax.swing.border.TitledBorder

import scalismo.ui.model.SceneNode
import scalismo.ui.model.properties.{ HasOpacity, NodeProperty }
import scalismo.ui.view.{ FancySlider, ScalismoFrame }

import scala.swing.BorderPanel
import scala.swing.event.ValueChanged

class OpacityPropertyPanel(override val frame: ScalismoFrame) extends BorderPanel with PropertyPanel {
  override def description: String = "Opacity"

  private var targets: List[HasOpacity] = Nil

  private val opacitySlider = new FancySlider {
    min = 0
    max = 100
    value = 100

    override def formattedValue(sliderValue: Int): String = s"$sliderValue%"
  }

  layout(new BorderPanel {
    val opacityPanel = new BorderPanel {
      border = new TitledBorder(null, description, TitledBorder.LEADING, 0, null, null)
      layout(opacitySlider) = BorderPanel.Position.Center
    }
    layout(opacityPanel) = BorderPanel.Position.Center
  }) = BorderPanel.Position.North

  listenToOwnEvents()

  def listenToOwnEvents() = {
    listenTo(opacitySlider)
  }

  def deafToOwnEvents() = {
    deafTo(opacitySlider)
  }

  def updateUi() = {
    deafToOwnEvents()
    targets.headOption.foreach(t => opacitySlider.value = (t.opacity.value * 100.0f).toInt)
    listenToOwnEvents()
  }

  override def setNodes(nodes: List[SceneNode]): Boolean = {
    cleanup()
    if (nodes.nonEmpty) {
      val ok = nodes.collect { case n: HasOpacity => n }
      if (ok.length == nodes.length) {
        targets = ok
        listenTo(targets.head.opacity)
        updateUi()
        return true
      }
    }
    false
  }

  def cleanup(): Unit = {
    targets.headOption.foreach(t => deafTo(t.opacity))
    targets = Nil
  }

  reactions += {
    case NodeProperty.event.PropertyChanged(_) => updateUi()
    case ValueChanged(c) => targets.foreach(_.opacity.value = opacitySlider.value.toFloat / 100.0f)
  }

}
