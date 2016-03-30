package scalismo.ui.model.properties

object PickableProperty {
  val DefaultValue: Boolean = true

}

class PickableProperty(initialValue: Boolean) extends NodeProperty[Boolean](initialValue) {
  def this() = this(PickableProperty.DefaultValue)

}

trait HasPickable {
  def pickable: PickableProperty
}
