package scalismo.ui.swing.actions.scenetree

import scalismo.ui._

class GotoLandmarkAction extends SceneTreePopupAction("Center slices here") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context match {
      case Some(lm: Landmark) => true
      case _ => false
    }
  }

  override def apply(context: Option[SceneTreeObject]) = {
    context match {
      case Some(lm: Landmark) =>
        lm.scene.slicingPosition.point = lm.point
      case _ =>
    }
  }
}
