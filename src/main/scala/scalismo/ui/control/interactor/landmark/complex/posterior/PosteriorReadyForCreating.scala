package scalismo.ui.control.interactor.landmark.complex.posterior

import java.awt.Cursor
import java.awt.event.MouseEvent

import scalismo.ui.control.interactor.Interactor.Verdict
import scalismo.ui.control.interactor.Interactor.Verdict.Block
import scalismo.ui.control.interactor.Recipe
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

  def transitionToEditing(modelLm: LandmarkNode, targetLm: LandmarkNode): Unit = {
    parent.transitionTo(PosteriorEditing.enter(modelLm, targetLm))
  }

  override def mouseClicked(e: MouseEvent): Verdict = {
    if (e.getButton == MouseEvent.BUTTON1) {
      parent.getLandmarkForClick(e) match {
        case Some((lm, group)) if group == interactor.sourceGpNode.group =>
          val modelLm = group.landmarks.add(lm.copy(id = group.landmarks.nameGenerator.nextName()))
          val targetLm = interactor.targetUncertaintyGroup.landmarks.add(lm.copy(id = modelLm.name))
          targetLm.pickable.value = false
          e.canvas.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR))
          transitionToEditing(modelLm, targetLm)
        case _ =>
      }
    }
    Block
  }

  override def mousePressed(e: MouseEvent): Verdict = Recipe.Block2DRotation.mousePressed(e)
}
