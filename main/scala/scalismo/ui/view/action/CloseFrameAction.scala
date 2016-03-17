package scalismo.ui.view.action

import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.view.ScalismoFrame

import scala.swing.Action

class CloseFrameAction(title: String = "Close")(implicit frame: ScalismoFrame) extends Action(title) {
  icon = BundledIcon.Exit.standardSized()

  override def apply(): Unit = {
    frame.closeOperation()
  }
}
