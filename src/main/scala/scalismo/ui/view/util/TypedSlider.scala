package scalismo.ui.view.util

import scalismo.ui.event.ScalismoPublisher
import java.awt.Color

import scala.swing.{Component, Label, Slider}
import scala.swing.event.{Event, ValueChanged}

class TypedSlider[A: Numeric](showLabels: Boolean = false, val stepSize: Double = 1.0)(
  implicit getValue: CanCreateFromDouble[A]
) extends ScalismoPublisher {

  val slider = new Slider()

  protected def toSliderValue(outer: Double): Int = {
    (outer / stepSize).round.toInt
  }

  protected def fromSliderValue(inner: Int): A = {
    getValue.fromDouble(inner * stepSize)
  }

  def up(): Unit = {
    if (slider.value < slider.max) {
      slider.value = slider.value + 1
    }
  }

  def down(): Unit = {
    if (slider.value > slider.min) {
      slider.value = slider.value - 1
    }
  }

  def min: A = fromSliderValue(slider.min)

  def min_=(value: Double) {
    slider.min = Math.floor(toSliderValue(value)).toInt
  }

  def max: A = fromSliderValue(slider.max)

  def max_=(value: Double) {
    slider.max = Math.ceil(toSliderValue(value)).toInt
  }

  def value: A = fromSliderValue(slider.value)

  def value_=(value: Double) {
    slider.value = toSliderValue(value)
  }

  // intended to be overwritten in subclasses if needed
  def formattedValue(sliderValue: A): String = sliderValue.toString

  val minLabel: Label = new Label {
    foreground = Color.GRAY
  }

  val maxLabel: Label = new Label {
    foreground = Color.GRAY
  }

  val valueLabel: Label = new Label {
    foreground = Color.BLACK
  }

  protected def updateLabels(): Unit = {
    if (slider.paintLabels) {
      val texts = Seq(min, max, value) map formattedValue
      val tuples = texts.zip(Seq(minLabel, maxLabel, valueLabel))
      val needsUpdate = tuples.exists { case (v, l) => l.text != v }
      if (needsUpdate) {
        tuples.foreach { case (v, l) => l.text = v }
        slider.labels =
          Seq((slider.min, minLabel), (slider.max, maxLabel), (slider.value, valueLabel)).filter(_._2.visible).toMap
      }
    }
  }

  def listenToOwnEvents(): Unit = listenTo(slider)

  def deafToOwnEvents(): Unit = deafTo(slider)

  reactions += {
    case ValueChanged(s) if s eq slider => {
      if (slider.paintLabels) updateLabels()
      publishEvent(new TypedSliderValueChanged(TypedSlider.this))
    }
  }

  listenToOwnEvents()

  slider.paintLabels = showLabels

}

case class TypedSliderValueChanged[A: Numeric](source: TypedSlider[A]) extends Event

trait CanCreateFromDouble[A] { def fromDouble(d: Double): A }

object CanCreateFromDouble {
  implicit object CanCreateIntFromDouble extends CanCreateFromDouble[Int] {
    def fromDouble(value: Double): Int = value.round.toInt
  }

  implicit object CanCreateFloatFromDouble extends CanCreateFromDouble[Float] {
    def fromDouble(value: Double): Float = value.toFloat
  }

  implicit object CanCreateDoubleFromDouble extends CanCreateFromDouble[Double] {
    def fromDouble(value: Double): Double = value.toDouble
  }
}
