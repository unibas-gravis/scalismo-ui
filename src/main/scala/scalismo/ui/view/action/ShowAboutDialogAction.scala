package scalismo.ui.view.action

import java.awt.Color

import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.dialog.AboutDialog

import scala.swing.Action

class ShowAboutDialogAction(name: String = "About")(implicit frame: ScalismoFrame) extends Action(name) {
  icon = BundledIcon.Information.colored(Color.BLACK).standardSized()

  override def apply(): Unit = {
    // the dialog takes care about showing itself correctly
    new AboutDialog()
  }

}
