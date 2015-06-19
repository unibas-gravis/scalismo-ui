package scalismo.ui.visualization

import scalismo.ui.EdtPublisher

import scala.swing.event.Event

// this object can be subscribed to to receive events when /any/ VisualizationProperty has changed,
// without having to subscribe to each property individually
object VisualizationProperty extends EdtPublisher {

  case class ValueChanged[V, C <: VisualizationProperty[V, C]](source: VisualizationProperty[V, C]) extends Event

  def publishValueChanged[V, C <: VisualizationProperty[V, C]](source: VisualizationProperty[V, C]) = {
    val event = ValueChanged(source)
    source.publishEdt(event)
    this.publishEdt(event)
  }
}

trait VisualizationProperty[V, C <: VisualizationProperty[V, C]] extends Derivable[C] with EdtPublisher {
  private var _value: Option[V] = None

  protected[ui] final def value: V = {
    _value.getOrElse(defaultValue)
  }

  final def apply(): V = value
  final def update(nv: V) = value = nv

  // hook to allow implementations to sanitize input (i.e., restrict to bounds)
  protected def sanitizeValue(newValue: V): V = newValue

  private def setSaneValue(saneValue: V): Unit = {
    if (saneValue != value) {
      _value = Some(saneValue)
      derived().foreach(_.setSaneValue(saneValue))
      VisualizationProperty.publishValueChanged(this)
    }
  }

  protected[ui] final def value_=(newValue: V): Unit = {
    setSaneValue(sanitizeValue(newValue))
  }

  def defaultValue: V

  final override protected def createDerived(): C = {
    val child = newInstance()
    child.setSaneValue(this.value)
    child
  }

  protected def newInstance(): C
}