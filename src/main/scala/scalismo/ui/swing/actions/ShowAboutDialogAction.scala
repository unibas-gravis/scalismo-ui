package scalismo.ui.swing.actions

import java.awt.Color

import scalismo.ui.swing.ScalismoFrame
import scalismo.ui.swing.util.AboutDialog

import scala.swing.Action

class ShowAboutDialogAction(name: String = "About")(implicit frame: ScalismoFrame) extends Action(name) {

  override def apply(): Unit = {
    // the dialog takes care about showing itself correctly
    new AboutDialog()(frame)
  }

}
