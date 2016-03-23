package scalismo.ui.control.interactor.landmark.complex

import java.awt.event.{ MouseWheelEvent, MouseEvent }

import scalismo.ui.control.interactor.Interactor.Verdict
import scalismo.ui.control.interactor.Interactor.Verdict.Block
import scalismo.ui.control.interactor.landmark.complex.ComplexLandmarkingInteractor.{ StateTransition, Delegate }
import scalismo.ui.model.LandmarkNode

object Editing {
  def enter[IT <: ComplexLandmarkingInteractor[IT], DT <: Delegate[IT]](lm: LandmarkNode): StateTransition[IT, DT] = editAxis(lm, 0)

  def editAxis[IT <: ComplexLandmarkingInteractor[IT], DT <: Delegate[IT]](lm: LandmarkNode, axisIndex: Int): StateTransition[IT, DT] = new StateTransition[IT, DT] {
    override def apply()(implicit parent: ComplexLandmarkingInteractor[IT]): Delegate[IT] = new Editing[IT](lm, axisIndex)
  }
}

class Editing[IT <: ComplexLandmarkingInteractor[IT]](landmarkNode: LandmarkNode, axisIndex: Int)(implicit override val parent: ComplexLandmarkingInteractor[IT]) extends ComplexLandmarkingInteractor.Delegate[IT] {
  println(s"editing axis: $axisIndex")

  override def onLandmarkCreationToggled(): Unit = cancel()

  override def mouseExited(e: MouseEvent): Verdict = {
    cancel()
    super.mouseExited(e)
  }

  override def mouseClicked(e: MouseEvent): Verdict = {
    if (e.getButton == MouseEvent.BUTTON2) {
      val nextAxisIndex = (axisIndex + 1) % 3
      transitionToEditAxis(landmarkNode, nextAxisIndex)
    } else if (e.getButton == MouseEvent.BUTTON3) {
      cancel()
    }

    Block
  }

  override def mouseWheelMoved(e: MouseWheelEvent): Verdict = {
    val more = {
      if (e.getWheelRotation < 0) true else false
    }
    // this provides a somewhat natural zoom feeling.
    val factor = 1.05f
    val sigmas = landmarkNode.uncertainty.value.sigmas.toArray
    val oldValue = sigmas(axisIndex)
    val newValue = {
      if (more) {
        oldValue * factor
      } else {
        oldValue / factor
      }
    }

    sigmas(axisIndex) = Math.max(0, newValue)
    landmarkNode.uncertainty.value = landmarkNode.uncertainty.value.copy(sigmas = sigmas.toList)
    Block
  }

  def cancel(): Unit = {
    println("Canceled landmark editing")
    if (parent.isLandmarkCreationEnabled) {
      transitionToReadyForCreating()
    } else {
      transitionToReadyForEditing()
    }
  }

  def transitionToReadyForCreating(): Unit = {
    parent.transitionTo(ReadyForCreating.enter)
  }

  def transitionToReadyForEditing(): Unit = {
    parent.transitionTo(ReadyForEditing.enter)
  }

  def transitionToEditAxis(node: LandmarkNode, axisIndex: Int): Unit = {
    parent.transitionTo(Editing.editAxis(landmarkNode, axisIndex))
  }
}
