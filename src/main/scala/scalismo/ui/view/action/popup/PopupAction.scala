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

package scalismo.ui.view.action.popup

import javax.swing.{Icon, JComponent}
import scalismo.ui.model.SceneNode
import scalismo.ui.util.NodeListFilters
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.util.ScalableUI

import scala.swing.Action
import scala.swing.Swing.EmptyIcon

sealed trait PopupActionable

object PopupAction {

  trait Factory extends NodeListFilters {
    def apply(nodes: List[SceneNode])(implicit frame: ScalismoFrame): List[PopupActionable]
  }

  val BuiltinFactories: List[Factory] = List(
    // the order here also defines the order in the popup menu, so arrange entries as needed
    ChildVisibilityAction,
    VisibilityAction,
    CenterOnLandmarkAction,
    AddGroupAction,
    AddRigidTransformationAction,
    GroupDelegatingAction,
    LoadStatisticalShapeModelAction,
    LoadStatisticalVolumeMeshModelAction,
    LoadLoadableAction,
    SaveSaveableAction,
    SaveLandmarksAction,
    RenameRenameableAction,
    RemoveRemoveablesAction
  )

  var _factories: List[Factory] = BuiltinFactories

  def factories: List[Factory] = _factories

  def addFactory(factory: Factory): Unit = {
    _factories = factory :: _factories
  }

  def removeFactory(factory: Factory): Unit = {
    _factories = _factories.filter(_ != factory)
  }

  def apply(context: List[SceneNode])(implicit frame: ScalismoFrame): List[PopupActionable] = {
    factories.flatMap(f => f(context))
  }
}

abstract class PopupAction(name: String, icon: Icon = EmptyIcon) extends Action(name) with PopupActionable {
  if (icon != EmptyIcon) {
    val scaledIcon = ScalableUI.standardSizedIcon(icon)
    super.icon_=(scaledIcon)
  }
}

abstract class PopupActionWithOwnMenu extends PopupActionable {
  def menuItem: JComponent
}
