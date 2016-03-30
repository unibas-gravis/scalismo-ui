package scalismo.ui.view.action.popup

import scalismo.ui.model.{ LandmarkNode, Scene, SceneNode }
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.view.ScalismoFrame

object CenterOnLandmarkAction extends PopupAction.Factory {
  override def apply(context: List[SceneNode])(implicit frame: ScalismoFrame): List[PopupAction] = {
    singleMatch[LandmarkNode](context).map(n => new CenterOnLandmarkAction(n)).toList
  }
}

class CenterOnLandmarkAction(node: LandmarkNode)(implicit val frame: ScalismoFrame) extends PopupAction("Center slices here", BundledIcon.Center) {
  override def apply(): Unit = {
    frame.sceneControl.slicingPosition.point = node.transformedSource.point
  }
}
