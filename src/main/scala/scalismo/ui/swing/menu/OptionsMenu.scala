package scalismo.ui.swing.menu

import scalismo.ui._
import scalismo.ui.swing.ScalismoFrame
import scalismo.ui.swing.actions.ShowDisplayScalingDialogAction

import scala.swing.event.{ ButtonClicked, Key }
import scala.swing.{ Menu, MenuItem, RadioMenuItem }

object OptionsMenu {
  val Name = "Options"
}

class OptionsMenu(implicit app: ScalismoFrame) extends Menu(OptionsMenu.Name) {
  contents += new PerspectiveMenu
  contents += new ShowDisplayScalingDialogItem()
}

object PerspectiveMenu {
  val Name = "Perspective"
}

class ShowDisplayScalingDialogItem(implicit val frame: ScalismoFrame) extends MenuItem(new ShowDisplayScalingDialogAction) {
  mnemonic = Key.D
}

class PerspectiveMenu(implicit app: ScalismoFrame) extends Menu(PerspectiveMenu.Name) {

  private class PerspectiveMenuItem(val factory: PerspectiveFactory) extends RadioMenuItem(factory.name) with EdtPublisher {
    listenTo(app.scene)
    reactions += {
      case ButtonClicked(b) =>
        if (app.scene.perspective.factory != factory) {
          app.scene.perspective = factory.apply()(app.scene)
        }
      case Scene.PerspectiveChanged(s) => updateUi()
    }
    updateUi()

    def updateUi() = {
      selected = app.scene.perspective.factory == factory
    }
  }

  Perspectives.availablePerspectives.foreach {
    pf =>
      contents += new PerspectiveMenuItem(pf)
  }

}
