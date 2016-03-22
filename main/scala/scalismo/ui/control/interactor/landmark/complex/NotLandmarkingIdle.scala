package scalismo.ui.control.interactor.landmark.complex

import java.awt.event.MouseEvent

import scalismo.ui.control.interactor.Interactor.Verdict
import scalismo.ui.control.interactor.Recipe
import scalismo.ui.model.{ LandmarkNode, SceneNode }

case class NotLandmarkingIdle(implicit override val parent: ComplexLandmarkingInteractor) extends ComplexLandmarkingInteractor.Delegate {
  override def onLandmarkCreationToggled(): Unit = {
    if (parent.isLandmarkCreationOn) {
      parent.switchState(ComplexLandmarkingInteractor.StateId.LandmarkingIdle, this)
    }
  }

  override def mouseMoved(e: MouseEvent): Verdict = {
    def onlyLandmarks(node: SceneNode) = node match {
      case yep: LandmarkNode => true
      case _ => false
    }
    Recipe.HighlightOutlineOfPickableObject.mouseMoved(e, onlyLandmarks)
  }
}
