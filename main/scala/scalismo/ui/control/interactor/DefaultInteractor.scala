package scalismo.ui.control.interactor

import java.awt.event.{ KeyEvent, MouseEvent, MouseWheelEvent }

import scalismo.ui.control.interactor.Interactor.Verdict
import scalismo.ui.control.interactor.Interactor.Verdict.Pass

trait DefaultInteractor extends Interactor {
  override def mousePressed(e: MouseEvent): Verdict = {
    Recipe.Block2DRotation.mousePressed(e)
  }

  override def mouseReleased(e: MouseEvent): Verdict = {
    Recipe.Block2DRotation.mouseReleased(e)
  }

  override def keyPressed(e: KeyEvent): Verdict = {
    Recipe.ShiftKeySetsSlicePosition.keyPressedOrReleased(e)
    Recipe.ControlKeyShowsImageInformation.keyPressedOrReleased(e)
    Pass
  }

  override def keyReleased(e: KeyEvent): Verdict = {
    Recipe.ShiftKeySetsSlicePosition.keyPressedOrReleased(e)
    Recipe.ControlKeyShowsImageInformation.keyPressedOrReleased(e)
    Pass
  }

  override def mouseMoved(e: MouseEvent): Verdict = {
    Recipe.ShiftKeySetsSlicePosition.mouseMoved(e)
    Recipe.ControlKeyShowsImageInformation.mouseMoved(e)
    Pass
  }

  override def mouseEntered(e: MouseEvent): Verdict = {
    Recipe.RequestFocusOnEnter.mouseEntered(e)
  }

  override def mouseWheelMoved(e: MouseWheelEvent): Verdict = {
    Recipe.Scroll2D.mouseWheelMoved(e)
  }
}
