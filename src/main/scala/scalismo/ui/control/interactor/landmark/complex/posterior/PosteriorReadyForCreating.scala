package scalismo.ui.control.interactor.landmark.complex.posterior

import java.awt.event.MouseEvent

import scalismo.ui.control.interactor.Interactor.Verdict
import scalismo.ui.control.interactor.Interactor.Verdict.Block
import scalismo.ui.control.interactor.landmark.complex.ComplexLandmarkingInteractor.{ Delegate, StateTransition }
import scalismo.ui.control.interactor.landmark.complex.{ ComplexLandmarkingInteractor, ReadyForCreating }
import scalismo.ui.model.LandmarkNode

object PosteriorReadyForCreating {
  def enter[InteractorType <: ComplexLandmarkingInteractor[InteractorType], DelegateType <: Delegate[InteractorType]]: StateTransition[InteractorType, DelegateType] = new StateTransition[InteractorType, DelegateType] {
    override def apply()(implicit parent: InteractorType) = new PosteriorReadyForCreating()
  }
}

class PosteriorReadyForCreating[InteractorType <: ComplexLandmarkingInteractor[InteractorType]](implicit parent: InteractorType) extends ReadyForCreating[InteractorType] {
  def interactor: PosteriorLandmarkingInteractor = parent.asInstanceOf[PosteriorLandmarkingInteractor]

  override def transitionToReadyForEditing(): Unit = {
    parent.transitionTo(PosteriorReadyForEditing.enter)
  }

  override def transitionToEditing(lm: LandmarkNode): Unit = {
    parent.transitionTo(PosteriorEditing.enter(lm))
  }

  override def mouseClicked(e: MouseEvent): Verdict = {
    if (e.getButton == MouseEvent.BUTTON1) {
      parent.getLandmarkForClick(e) match {
        case Some((lm, group)) if group == interactor.sourceGpNode.group =>
          val node = group.landmarks.add(lm.copy(id = group.landmarks.nameGenerator.nextName()))
          transitionToEditing(node)
        case _ =>
      }
    }
    Block
  }

}
