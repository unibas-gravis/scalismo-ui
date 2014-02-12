package org.statismo.stk.ui.swing.menu

import scala.swing.Menu
import scala.swing.MenuItem
import org.statismo.stk.ui.swing.actions.AboutAction
import org.statismo.stk.ui.Perspectives
import org.statismo.stk.ui.StatismoFrame
import scala.swing.Action
import scala.swing.RadioMenuItem
import org.statismo.stk.ui.PerspectiveFactory
import scala.swing.event.ButtonClicked
import org.statismo.stk.ui.Scene

object OptionsMenu {
  val Name = "Options"
}

class OptionsMenu(implicit app: StatismoFrame) extends Menu(OptionsMenu.Name) {
  contents += new PerspectiveMenu
}

object PerspectiveMenu {
  val Name = "Perspective"
}

class PerspectiveMenu(implicit app: StatismoFrame) extends Menu(PerspectiveMenu.Name) {
  
  private class PerspectiveMenuItem(val factory: PerspectiveFactory) extends RadioMenuItem(factory.Name) {
    listenTo(app.scene)
    reactions += {
      case ButtonClicked(b) => {
        if (app.scene.perspective.factory != factory) {
        	app.scene.perspective = factory.apply(app.scene)
        }
      }
      case Scene.PerspectiveChanged(s) => updateUi
    }
    updateUi
    def updateUi = {
      selected = app.scene.perspective.factory == factory
    }
  }
  
  Perspectives.availablePerspectives.foreach {pf =>
    contents += new PerspectiveMenuItem(pf)
  }
  
}