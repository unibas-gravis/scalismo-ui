package scalismo.ui.model.capabilities

trait Renameable {
  private var _name: String = null

  def name = if (_name == null) "(null)" else _name

  def name_=(newValue: String) = {
    _name = newValue
  }
}
