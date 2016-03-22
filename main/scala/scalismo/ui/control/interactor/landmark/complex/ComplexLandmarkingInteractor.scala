package scalismo.ui.control.interactor.landmark.complex

import java.awt.Color

import scalismo.ui.control.interactor.landmark.complex.ComplexLandmarkingInteractor.StateId
import scalismo.ui.control.interactor.{ DelegatedInteractor, DelegatingInteractor }
import scalismo.ui.event.ScalismoPublisher
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.view.ScalismoFrame

import scala.swing.ToggleButton
import scala.swing.event.ButtonClicked

object ComplexLandmarkingInteractor {
  trait Delegate extends DelegatedInteractor[ComplexLandmarkingInteractor] {
    def onLandmarkCreationToggled()
  }

  class StateId()

  object StateId {
    val NotLandmarkingIdle = new StateId()
    val LandmarkingIdle = new StateId()
  }
}

trait ComplexLandmarkingInteractor extends DelegatingInteractor[ComplexLandmarkingInteractor] with ScalismoPublisher {

  override type Delegate = ComplexLandmarkingInteractor.Delegate
  implicit lazy val myself = this

  private val landmarkingButton = new ToggleButton {
    val myIcon = BundledIcon.Landmark

    def updateUi() = {
      val onOff = if (selected) "ON" else "OFF"
      tooltip = s"Toggle landmarking (currently $onOff)"
      val iconColor = if (selected) Color.GREEN.darker else Color.DARK_GRAY
      icon = myIcon.colored(iconColor).standardSized()
    }

    reactions += {
      case ButtonClicked(_) => updateUi()
    }

    updateUi()
  }

  override protected def initialDelegate: Delegate = NotLandmarkingIdle()

  override def onActivated(frame: ScalismoFrame): Unit = {
    frame.toolbar.add(landmarkingButton)
  }

  override def onDeactivated(frame: ScalismoFrame): Unit = {
    frame.toolbar.remove(landmarkingButton)
  }

  def switchState(to: StateId, from: ComplexLandmarkingInteractor.Delegate): Unit = {
    delegate = selectState(to, from)
  }

  protected def selectState(to: StateId, from: ComplexLandmarkingInteractor.Delegate): Delegate = {
    val r = to match {
      case StateId.NotLandmarkingIdle => NotLandmarkingIdle()
      case StateId.LandmarkingIdle => LandmarkingIdle()
      case _ => throw new IllegalStateException()
    }
    println(r)
    r
  }

  listenTo(landmarkingButton)

  def isLandmarkCreationOn: Boolean = landmarkingButton.selected

  reactions += {
    case ButtonClicked(`landmarkingButton`) => delegate.onLandmarkCreationToggled()
  }
}
