package scalismo.ui.view.action.popup

import javax.swing.{ Icon, JComponent }

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
    VisibilityAction,
    CenterOnLandmarkAction,
    AddGroupAction,
    AddRigidTransformationAction,
    LoadStatisticalShapeModelAction,
    GroupDelegatingAction,
    LoadLoadableAction,
    SaveSaveableAction,
    SaveLandmarksAction,
    RenameRenameableAction,
    RemoveRemoveablesAction
  )

  var _factories: List[Factory] = BuiltinFactories

  def factories: List[Factory] = _factories

  def addFactory(factory: Factory) = {
    _factories = factory :: _factories
  }

  def removeFactory(factory: Factory) = {
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
