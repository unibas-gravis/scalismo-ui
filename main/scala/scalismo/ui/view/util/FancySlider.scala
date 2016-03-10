package scalismo.ui.view.util

import java.awt.Color

import scala.swing.event.ValueChanged
import scala.swing.{ Label, Slider }

class FancySlider extends Slider {
  // intended to be overwritten in subclasses if needed
  def formattedValue(sliderValue: Int): String = sliderValue.toString

  // The labels are protected so that subclasses can override properties like color etc.
  // When setting the visible property of a label to false, it won't be shown on the slider (duh! :-) )
  protected val minLabel = new Label {
    foreground = Color.GRAY
  }

  protected val maxLabel = new Label {
    foreground = Color.GRAY
  }

  protected val valueLabel = new Label {
    foreground = Color.BLACK
  }

  protected def updateLabels(): Unit = {
    if (paintLabels) {
      val texts = Seq(min, max, value) map formattedValue
      val tuples = texts.zip(Seq(minLabel, maxLabel, valueLabel))
      val needsUpdate = tuples.exists { case (v, l) => l.text != v }
      if (needsUpdate) {
        tuples.foreach { case (v, l) => l.text = v }
        labels = Seq((min, minLabel), (max, maxLabel), (value, valueLabel)).filter(_._2.visible).toMap
      }
    }
  }

  paintLabels = true

  reactions += {
    case ValueChanged(c) if c eq this => updateLabels()
  }
}
