package scalismo.ui.view.properties

import java.awt.Color
import javax.swing.border.TitledBorder

import scalismo.ui.model.SceneNode
import scalismo.ui.model.properties.{ HasScalarRange, NodeProperty }
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.util.FancySlider

import scala.swing.BorderPanel
import scala.swing.event.ValueChanged

object ScalarRangePropertyPanel extends PropertyPanel.Factory {
  override def create(frame: ScalismoFrame): PropertyPanel = {
    new ScalarRangePropertyPanel(frame)
  }
}

class ScalarRangePropertyPanel(override val frame: ScalismoFrame) extends BorderPanel with PropertyPanel {
  override def description: String = "Scalar Range"

  private var targets: List[HasScalarRange] = Nil
  private var min: Float = 0
  private var max: Float = 100
  private var step: Float = 1

  private val minimumSlider = new FancySlider {
    min = 0
    max = 100
    value = 0

    valueLabel.foreground = Color.RED.darker()

    override def formattedValue(sliderValue: Int): String = formatSliderValue(sliderValue)
  }

  private val maximumSlider = new FancySlider {
    min = 0
    max = 100
    value = 100

    valueLabel.foreground = Color.BLUE.darker()

    override def formattedValue(sliderValue: Int): String = formatSliderValue(sliderValue)
  }

  {
    val northedPanel = new BorderPanel {
      val slidersPanel = new BorderPanel {
        border = new TitledBorder(null, description, TitledBorder.LEADING, 0, null, null)
        layout(minimumSlider) = BorderPanel.Position.North
        layout(maximumSlider) = BorderPanel.Position.Center
      }
      layout(slidersPanel) = BorderPanel.Position.Center
    }
    layout(northedPanel) = BorderPanel.Position.North
  }

  listenToOwnEvents()

  def listenToOwnEvents() = {
    listenTo(minimumSlider, maximumSlider)
  }

  def deafToOwnEvents() = {
    deafTo(minimumSlider, maximumSlider)
  }

  def toSliderValue(v: Float): Int = {
    if (step == 0) 0 else Math.round((v - min) / step)
  }

  def formatSliderValue(i: Int): String = {
    if (step == 0) "0"
    else if (step >= 1) f"${fromSliderValue(i)}%2.0f"
    else if (step >= .1) f"${fromSliderValue(i)}%2.1f"
    else if (step >= .01) f"${fromSliderValue(i)}%2.2f"
    else f"${fromSliderValue(i)}%2.3f"
  }

  def fromSliderValue(v: Int): Float = {
    v * step + min
  }

  def updateUi() = {
    deafToOwnEvents()
    targets.foreach { t =>
      min = t.scalarRange.value.absoluteMinimum
      max = t.scalarRange.value.absoluteMaximum
      step = (max - min) / 100.0f

      // this is an ugly workaround to make sure (min, max) values are properly displayed
      def reinitSlider(s: FancySlider) = {
        s.min = 1
        s.min = 0
        s.max = 99
        s.max = 100
      }
      reinitSlider(minimumSlider)
      reinitSlider(maximumSlider)

      minimumSlider.value = toSliderValue(t.scalarRange.value.cappedMinimum)
      maximumSlider.value = toSliderValue(t.scalarRange.value.cappedMaximum)

    }
    listenToOwnEvents()
  }

  override def setNodes(nodes: List[SceneNode]): Boolean = {
    cleanup()
    val supported = allMatch[HasScalarRange](nodes)
    if (supported.nonEmpty) {
      targets = supported
      listenTo(targets.head.scalarRange)
      updateUi()
      true
    } else false
  }

  def cleanup(): Unit = {
    targets.foreach(t => deafTo(t.scalarRange))
    targets = Nil
  }

  reactions += {
    case NodeProperty.event.PropertyChanged(_) => updateUi()
    case ValueChanged(slider) =>
      deafToOwnEvents()
      if (maximumSlider.value < minimumSlider.value) {
        if (slider eq minimumSlider) maximumSlider.value = minimumSlider.value
        else if (slider eq maximumSlider) minimumSlider.value = maximumSlider.value
      }
      propagateSliderChanges()
      listenToOwnEvents()
    //target.foreach(_.opacity.value = minimumSlider.value.toFloat / 100.0f)
  }

  def propagateSliderChanges(): Unit = {
    val (fMin, fMax) = (fromSliderValue(minimumSlider.value), fromSliderValue(maximumSlider.value))
    targets.foreach(t => t.scalarRange.value = t.scalarRange.value.copy(cappedMinimum = fMin, cappedMaximum = fMax))
  }

}
