package scalismo.ui.control.interactor.landmark.complex

import java.awt.{ Cursor, Color }
import java.awt.event.MouseEvent

import scalismo.ui.control.interactor.Interactor.Verdict
import scalismo.ui.control.interactor.landmark.complex.ComplexLandmarkingInteractor.{ Delegate, StateTransition }
import scalismo.ui.control.interactor.{ DefaultInteractor, DelegatedInteractor, DelegatingInteractor }
import scalismo.ui.event.ScalismoPublisher
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.view.ScalismoFrame

import scala.swing.ToggleButton
import scala.swing.event.ButtonClicked

object ComplexLandmarkingInteractor {

  trait Delegate[InteractorType <: ComplexLandmarkingInteractor[InteractorType]] extends DelegatedInteractor[ComplexLandmarkingInteractor[InteractorType]] {
    def onLandmarkCreationToggled()

  }

  trait StateTransition[InteractorType <: ComplexLandmarkingInteractor[InteractorType], DelegateType <: Delegate[InteractorType]] {
    def apply()(implicit parent: ComplexLandmarkingInteractor[InteractorType]): Delegate[InteractorType]
  }

  //FIXME
  class Example(override val frame: ScalismoFrame) extends DefaultInteractor with ComplexLandmarkingInteractor[Example] {

  }
}

trait ComplexLandmarkingInteractor[InteractorType <: ComplexLandmarkingInteractor[InteractorType]] extends DelegatingInteractor[ComplexLandmarkingInteractor[InteractorType]] with ScalismoPublisher {

  implicit lazy val myself = this

  private lazy val landmarkingButton = new ToggleButton {
    selected = true
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

  override protected def initialDelegate = {
    if (isLandmarkCreationEnabled) {
      ReadyForCreating.enter()
    } else {
      ReadyForEditing.enter()
    }
  }

  override def onActivated(frame: ScalismoFrame): Unit = {
    frame.toolbar.add(landmarkingButton)
  }

  override def onDeactivated(frame: ScalismoFrame): Unit = {
    frame.toolbar.remove(landmarkingButton)
  }

  // set the cursor to a crosshair if we're in landmarking mode
  override def mouseEntered(e: MouseEvent): Verdict = {
    val cursor = if (landmarkingButton.selected) Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) else Cursor.getDefaultCursor
    e.canvas.setCursor(cursor)
    super.mouseEntered(e)
  }

  def transitionTo(transition: StateTransition[InteractorType, _ <: Delegate[InteractorType]]): Unit = {
    val n = transition.apply()
    println("transitioning to: " + n.getClass)
    delegate = n
  }

  listenTo(landmarkingButton)

  def isLandmarkCreationEnabled: Boolean = landmarkingButton.selected

  reactions += {
    case ButtonClicked(`landmarkingButton`) =>
      val del = delegate: Delegate[InteractorType]
      del.onLandmarkCreationToggled()
  }
}
