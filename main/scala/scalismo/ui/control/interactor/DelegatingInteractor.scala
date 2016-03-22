package scalismo.ui.control.interactor

import java.awt.event.{ KeyEvent, MouseEvent, MouseWheelEvent }

import scalismo.ui.control.interactor.Interactor.Verdict
import scalismo.ui.control.interactor.Interactor.Verdict.Block
import scalismo.ui.view.ScalismoFrame

trait DelegatedInteractor[P <: DelegatingInteractor[P]] extends Interactor {
  def parent: DelegatingInteractor[P]

}

trait DelegatingInteractor[P <: DelegatingInteractor[P]] extends Interactor { self =>
  def frame: ScalismoFrame

  type Delegate <: DelegatedInteractor[P]

  var _delegate: Delegate = initialDelegate

  protected def initialDelegate: Delegate

  def delegate: Delegate = _delegate
  def delegate_=(newDelegate: Delegate): Unit = {
    if (newDelegate != _delegate) {
      _delegate.onDeactivated(frame)
      _delegate = newDelegate
      _delegate.onActivated(frame)
    }
  }

  override def keyPressed(e: KeyEvent): Verdict = {
    if (delegate.keyPressed(e) == Block) Block else super.keyPressed(e)
  }

  override def keyReleased(e: KeyEvent): Verdict = {
    if (delegate.keyReleased(e) == Block) Block else super.keyReleased(e)
  }

  override def keyTyped(e: KeyEvent): Verdict = {
    if (delegate.keyTyped(e) == Block) Block else super.keyTyped(e)
  }

  override def mouseClicked(e: MouseEvent): Verdict = {
    if (delegate.mouseClicked(e) == Block) Block else super.mouseClicked(e)
  }

  override def mouseDragged(e: MouseEvent): Verdict = {
    if (delegate.mouseDragged(e) == Block) Block else super.mouseDragged(e)
  }

  override def mouseEntered(e: MouseEvent): Verdict = {
    if (delegate.mouseEntered(e) == Block) Block else super.mouseEntered(e)
  }

  override def mouseExited(e: MouseEvent): Verdict = {
    if (delegate.mouseExited(e) == Block) Block else super.mouseExited(e)
  }

  override def mouseMoved(e: MouseEvent): Verdict = {
    if (delegate.mouseMoved(e) == Block) Block else super.mouseMoved(e)
  }

  override def mousePressed(e: MouseEvent): Verdict = {
    if (delegate.mousePressed(e) == Block) Block else super.mousePressed(e)
  }

  override def mouseReleased(e: MouseEvent): Verdict = {
    if (delegate.mouseReleased(e) == Block) Block else super.mouseReleased(e)
  }

  override def mouseWheelMoved(e: MouseWheelEvent): Verdict = {
    if (delegate.mouseWheelMoved(e) == Block) Block else super.mouseWheelMoved(e)
  }

  override def onActivated(frame: ScalismoFrame): Unit = super.onActivated(frame)

  override def onDeactivated(frame: ScalismoFrame): Unit = super.onDeactivated(frame)
}
