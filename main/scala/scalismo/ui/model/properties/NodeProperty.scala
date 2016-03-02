package scalismo.ui.model.properties

import scalismo.ui.event.{ Event, ScalismoPublisher }
import scalismo.ui.model.properties.NodeProperty.event.PropertyChanged

object NodeProperty {
  object event {
    case class PropertyChanged[V](property: NodeProperty[V]) extends Event
  }
}

class NodeProperty[V](initialValue: => V) extends ScalismoPublisher {

  /**
   * Sanitize a value so that it fits into the expected value domain.
   *
   * For instance, the OpacityProperty sanitizes input values to be in [0,1].
   * @param possiblyNotSane a value, possibly not a sane one
   * @return the sanitized version of the value
   */
  protected def sanitize(possiblyNotSane: V) = possiblyNotSane

  private var _value: V = sanitize(initialValue)

  def value: V = _value

  def value_=(newValue: V) = {
    _value = sanitize(newValue)
    publishEdt(PropertyChanged(this))
  }

  override def toString: String = s"${getClass.getName}[$value]"
}
