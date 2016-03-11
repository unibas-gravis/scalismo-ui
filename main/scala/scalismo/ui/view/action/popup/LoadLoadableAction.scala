package scalismo.ui.view.action.popup

import scalismo.ui.model.SceneNode
import scalismo.ui.model.capabilities.Loadable
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.action.LoadAction

object LoadLoadableAction extends PopupAction.Factory {
  override def apply(context: List[SceneNode])(implicit frame: ScalismoFrame): List[PopupAction] = {
    singleNode[Loadable](context).map(l => new LoadLoadableAction(l)).toList
  }
}

class LoadLoadableAction(l: Loadable)(implicit val frame: ScalismoFrame) extends PopupAction(s"Load ${l.loadMetadata.description}...") {
  override def apply(): Unit = new LoadAction(l.load, l.loadMetadata, title).apply()
}
