package scalismo.ui.control.interactor.landmark.complex.posterior

import scalismo.ui.control.interactor.DefaultInteractor
import scalismo.ui.control.interactor.landmark.complex.ComplexLandmarkingInteractor.{ Delegate, StateTransition }
import scalismo.ui.control.interactor.landmark.complex.{ ReadyForCreating, ReadyForEditing, ComplexLandmarkingInteractor }
import scalismo.ui.view.ScalismoFrame

// This is only a proof-of-concept class to see if the ComplexLandmarkingInteractor can be selectively extended.
// It will go away soon.
class PosteriorLandmarkingInteractor(override val frame: ScalismoFrame) extends ComplexLandmarkingInteractor[PosteriorLandmarkingInteractor] {

  object PReadyForCreating {
    def enter[InteractorType <: ComplexLandmarkingInteractor[InteractorType], DelegateType <: Delegate[InteractorType]]: StateTransition[InteractorType, DelegateType] = new StateTransition[InteractorType, DelegateType] {
      override def apply()(implicit parent: ComplexLandmarkingInteractor[InteractorType]) = new PReadyForCreating()
    }
  }

  class PReadyForCreating[InteractorType <: ComplexLandmarkingInteractor[InteractorType]](implicit parent: ComplexLandmarkingInteractor[InteractorType]) extends ReadyForCreating[InteractorType] {
    println("PRC")
    override def transitionToReadyForEditing(): Unit = {
      parent.transitionTo(PReadyForEditing.enter)
    }
  }

  object PReadyForEditing {
    def enter[InteractorType <: ComplexLandmarkingInteractor[InteractorType], DelegateType <: Delegate[InteractorType]]: StateTransition[InteractorType, DelegateType] = new StateTransition[InteractorType, DelegateType] {
      override def apply()(implicit parent: ComplexLandmarkingInteractor[InteractorType]) = new PReadyForEditing()
    }
  }

  class PReadyForEditing[InteractorType <: ComplexLandmarkingInteractor[InteractorType]](implicit parent: ComplexLandmarkingInteractor[InteractorType]) extends ReadyForEditing[InteractorType] {
    println("PRE")
    override def transitionToReadyForCreating(): Unit = {
      parent.transitionTo(PReadyForCreating.enter)
    }
  }

  override protected def initialDelegate: Delegate[PosteriorLandmarkingInteractor] = {
    PReadyForCreating.enter()
  }

  override def transitionTo(transition: StateTransition[PosteriorLandmarkingInteractor, _ <: Delegate[PosteriorLandmarkingInteractor]]): Unit = {
    println(transition)
    super.transitionTo(transition)
  }
}