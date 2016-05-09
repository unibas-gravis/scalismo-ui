package scalismo.ui.api

import scalismo.ui.model.StatusMessage
import scalismo.ui.util.EdtUtil

import scala.swing.Component

trait SimplePluginAPI {

  def ui: ScalismoUI

  def activate(): Unit = {

    onActivated()
  }

  def deactivate(): Unit = {
    onDeactivated()
  }

  def message(message: String) = {
    ui.frame.status.set(StatusMessage(message))
  }

  def message(message: StatusMessage) = {
    ui.frame.status.set(StatusMessage(message.text, message.kind))
  }

  def addToToolbar(panel: Component): Unit = {
    EdtUtil.onEdt {
      ui.frame.toolbar.add(panel)
    }
  }

  def removeFromToolbar(panel: Component): Unit = {
    EdtUtil.onEdt {
      ui.frame.toolbar.remove(panel)
    }
  }

  def onActivated(): Unit

  def onDeactivated(): Unit

}
