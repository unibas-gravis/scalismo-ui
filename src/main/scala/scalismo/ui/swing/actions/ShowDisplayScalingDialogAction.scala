package scalismo.ui.swing.actions

import javax.swing.ImageIcon

import scalismo.ui.resources.icons.IconResources
import scalismo.ui.swing.ScalismoFrame
import scalismo.ui.swing.util.DisplayScalingDialog

import scala.swing.Action

class ShowDisplayScalingDialogAction(name: String = "Display Scaling")(implicit val frame: ScalismoFrame) extends Action(name) {

  override def apply(): Unit = {
    new DisplayScalingDialog().visible = true
  }
}
