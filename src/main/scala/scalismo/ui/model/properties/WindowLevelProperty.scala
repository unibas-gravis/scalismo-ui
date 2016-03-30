package scalismo.ui.model.properties

object WindowLevelProperty {
  val DefaultValue: WindowLevel = WindowLevel(256, 128)

}

class WindowLevelProperty(initialValue: WindowLevel) extends NodeProperty[WindowLevel](initialValue) {
  def this() = this(WindowLevelProperty.DefaultValue)

}

trait HasWindowLevel {
  def windowLevel: WindowLevelProperty
}
