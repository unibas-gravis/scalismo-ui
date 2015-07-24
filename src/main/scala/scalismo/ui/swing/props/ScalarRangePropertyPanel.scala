package scalismo.ui.swing.props

import javax.swing.border.TitledBorder

import scalismo.ui.swing.util.FancySlider
import scalismo.ui.visualization.VisualizationProperty
import scalismo.ui.visualization.props.{ HasScalarRange, HasOpacity }

import scala.swing.BorderPanel
import scala.swing.event.ValueChanged

class ScalarRangePropertyPanel extends BorderPanel with PropertyPanel {
  override def description: String = "Scalar Range"

  private var target: Option[HasScalarRange] = None
  private var min: Float = 0
  private var max: Float = 100
  private var step: Float = 1

  private val minimumSlider = new FancySlider {
    min = 0
    max = 100
    value = 0

    override def formattedValue(sliderValue: Int): String = formatSliderValue(sliderValue)
  }
  private val maximumSlider = new FancySlider {
    min = 0
    max = 100
    value = 100

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
    if (target.isDefined) {
      deafToOwnEvents()
      target.foreach { t =>
        min = t.scalarRange.value.absoluteMinimum
        max = t.scalarRange.value.absoluteMaximum
        step = (max - min) / 100.0f
        minimumSlider.value = toSliderValue(t.scalarRange.value.cappedMinimum)
        maximumSlider.value = toSliderValue(t.scalarRange.value.cappedMaximum)
        minimumSlider.revalidate()
        minimumSlider.repaint()
        maximumSlider.revalidate()
        maximumSlider.repaint()
      }
      listenToOwnEvents()
    }
  }

  override def setObject(obj: Option[AnyRef]): Boolean = {
    cleanup()
    obj match {
      case Some(c: HasScalarRange) =>
        target = Some(c)
        listenTo(c.scalarRange)
        updateUi()
        true
      case _ => false
    }
  }

  def cleanup(): Unit = {
    target.foreach(t => deafTo(t.scalarRange))
    target = None
  }

  reactions += {
    case VisualizationProperty.ValueChanged(_) => updateUi()
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
    target.foreach(t => t.scalarRange.value = t.scalarRange.value.copy(cappedMinimum = fMin, cappedMaximum = fMax))
  }

}
