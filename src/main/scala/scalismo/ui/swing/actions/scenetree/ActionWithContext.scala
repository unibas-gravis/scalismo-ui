package scalismo.ui.swing.actions.scenetree

import scala.swing.Action

abstract class ActionWithContext[C <: AnyRef](name: String) extends Action(name) {
  private var _context: Option[C] = None

  def setContext(context: Option[C]): Boolean = {
    _context = context
    isContextSupported(context)
  }

  def isContextSupported(context: Option[C]): Boolean

  override def apply() = {
    apply(_context)
  }

  def apply(context: Option[C]): Unit
}