package scalismo.ui.control.interactor.landmark.complex.posterior

import scalismo.ui.control.interactor.landmark.complex.ComplexLandmarkingInteractor.{ Delegate, StateTransition }
import scalismo.ui.control.interactor.landmark.complex.{ ComplexLandmarkingInteractor, ReadyForEditing }
import scalismo.ui.model.LandmarkNode

object PosteriorReadyForEditing {
  def enter[InteractorType <: ComplexLandmarkingInteractor[InteractorType], DelegateType <: Delegate[InteractorType]]: StateTransition[InteractorType, DelegateType] = new StateTransition[InteractorType, DelegateType] {
    override def apply()(implicit parent: InteractorType) = new PosteriorReadyForEditing()
  }
}

class PosteriorReadyForEditing[InteractorType <: ComplexLandmarkingInteractor[InteractorType]](implicit parent: InteractorType) extends ReadyForEditing[InteractorType] {

  override def transitionToReadyForCreating(): Unit = {
    parent.transitionTo(PosteriorReadyForCreating.enter)
  }

  def transitionToEditing(modelLm: LandmarkNode, targetLm: LandmarkNode): Unit = {
    parent.transitionTo(PosteriorEditing.enter(modelLm, targetLm))
  }
}

