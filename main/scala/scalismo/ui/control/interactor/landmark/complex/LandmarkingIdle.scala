package scalismo.ui.control.interactor.landmark.complex

import java.awt.event.MouseEvent

import scalismo.ui.control.interactor.Interactor.Verdict
import scalismo.ui.control.interactor.Recipe
import scalismo.ui.model.{ LandmarkNode, SceneNode }

case class LandmarkingIdle(implicit override val parent: ComplexLandmarkingInteractor) extends ComplexLandmarkingInteractor.Delegate {
  override def onLandmarkCreationToggled(): Unit = {
    if (!parent.isLandmarkCreationOn) {
      parent.switchState(ComplexLandmarkingInteractor.StateId.NotLandmarkingIdle, this)
    }
  }

  override def mouseMoved(e: MouseEvent): Verdict = {
    def exceptLandmarks(node: SceneNode) = node match {
      case nope: LandmarkNode => false
      case _ => true
    }
    Recipe.HighlightOutlineOfPickableObject.mouseMoved(e, exceptLandmarks)
  }

}
