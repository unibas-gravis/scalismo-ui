/*
 * Copyright (C) 2016  University of Basel, Graphics and Vision Research Group 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scalismo.ui.view.menu

import scalismo.ui.event.ScalismoPublisher
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.view.action.{ ShowBackgroundColorDialogAction, ShowDisplayScalingDialogAction }
import scalismo.ui.view.perspective.PerspectiveFactory
import scalismo.ui.view.{ PerspectivePanel, ScalismoFrame }

import scala.swing.event.{ ButtonClicked, Key }
import scala.swing.{ Menu, MenuItem, RadioMenuItem }

class ViewMenu extends Menu("View") {
  mnemonic = Key.V
}

object ViewMenu {

  class ShowDisplayScalingDialogItem(implicit val frame: ScalismoFrame) extends MenuItem(new ShowDisplayScalingDialogAction) {
    mnemonic = Key.D
  }

  class ShowBackgroundColorDialogItem(implicit val frame: ScalismoFrame) extends MenuItem(new ShowBackgroundColorDialogAction) {
    mnemonic = Key.B
  }

  class PerspectiveMenu(implicit val frame: ScalismoFrame) extends Menu("Perspective") {
    mnemonic = Key.P
    icon = BundledIcon.Perspective.standardSized()

    private val panel = frame.perspective

    private class PerspectiveMenuItem(val factory: PerspectiveFactory) extends RadioMenuItem(factory.perspectiveName) with ScalismoPublisher {

      def updateUi(): Unit = {
        selected = panel.perspective == factory
      }

      listenTo(panel)

      reactions += {
        case ButtonClicked(_) => panel.perspective = factory
        case PerspectivePanel.event.PerspectiveChanged(_, _, _) => updateUi()
      }

      updateUi()
    }

    PerspectiveFactory.factories.foreach { pf =>
      contents += new PerspectiveMenuItem(pf)
    }

  }

}

