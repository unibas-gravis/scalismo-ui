package scalismo.ui.model.properties

import java.awt.Color

import scalismo.ui.event.{ Event, ScalismoPublisher }

object ColorProperty extends ScalismoPublisher {
  val DefaultValue: Color = Color.WHITE

  object event {

    /* this is a simple "shortcut" event that gets published whenever *any* color property is changed.
    * It's used for keeping the Nodes View updated, without that view having to listen to all colorable objects
    */
    case object SomeColorPropertyChanged extends Event

  }

}

class ColorProperty(initialValue: Color) extends NodeProperty[Color](initialValue) {
  def this() = this(ColorProperty.DefaultValue)

  override def value_=(newValue: Color) = {
    super.value_=(newValue)
    ColorProperty.publishEvent(ColorProperty.event.SomeColorPropertyChanged)
  }
}

trait HasColor {
  def color: ColorProperty
}
