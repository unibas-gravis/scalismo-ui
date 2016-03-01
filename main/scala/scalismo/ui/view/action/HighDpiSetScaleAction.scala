package scalismo.ui.view.action

import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.dialog.HighDpiSetScaleDialog

import scala.swing.Action

class HighDpiSetScaleAction(val name: String)(implicit val frame: ScalismoFrame) extends Action(name) {
  override def apply(): Unit = {
    new HighDpiSetScaleDialog().visible = true
  }
}
