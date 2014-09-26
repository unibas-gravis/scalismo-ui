package org.statismo.stk.ui.swing.actions.scenetree

import scala.swing.Action

import scala.async.Async.async
import scala.concurrent.ExecutionContext.Implicits.global


abstract class ActionWithContext[C <: AnyRef](name: String) extends Action(name) {
  private var _context: Option[C] = None

  def setContext(context: Option[C]): Boolean = {
    _context = context
    isContextSupported(context)
  }

  def isContextSupported(context: Option[C]): Boolean

  override def apply() = {
    async { apply(_context) }
  }

  def apply(context: Option[C])
}