package scalismo.ui.view.action

import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.dialog.DisplayScalingDialog

import scala.swing.Action

class ShowDisplayScalingDialogAction(name: String = "Display Scaling")(implicit val frame: ScalismoFrame) extends Action(name) {
  icon = BundledIcon.Scale.standardSized()

  override def apply(): Unit = {
    new DisplayScalingDialog().visible = true
  }
}
