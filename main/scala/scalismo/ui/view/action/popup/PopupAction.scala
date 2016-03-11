package scalismo.ui.view.action.popup

import scalismo.ui.model.SceneNode
import scalismo.ui.util.NodeListFilters
import scalismo.ui.view.ScalismoFrame

import scala.swing.Action

object PopupAction {
  trait Factory extends NodeListFilters {
    def apply(context: List[SceneNode])(implicit frame: ScalismoFrame): Option[PopupAction]
  }

  val BuiltinFactories: List[Factory] = List(SaveSaveableAction)

  var _factories: List[Factory] = BuiltinFactories

  def factories: List[Factory] = _factories

  def addFactory(factory: Factory) = {
    _factories = factory :: _factories
  }

  def removeFactory(factory: Factory) = {
    _factories = _factories.filter(_ != factory)
  }

  def apply(context: List[SceneNode])(implicit frame: ScalismoFrame): List[PopupAction] = {
    factories.flatMap(f => f(context))
  }
}

abstract class PopupAction(name: String) extends Action(name) {

}
