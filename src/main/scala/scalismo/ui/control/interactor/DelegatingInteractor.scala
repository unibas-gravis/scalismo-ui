/*
 * Copyright (C) 2016  University of Basel, Graphics and Vision Research Group
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scalismo.ui.control.interactor

import java.awt.event.{KeyEvent, MouseEvent, MouseWheelEvent}

import scalismo.ui.control.interactor.Interactor.Verdict
import scalismo.ui.control.interactor.Interactor.Verdict.Block
import scalismo.ui.view.ScalismoFrame

//trait Parent[P <: Parent[P]] {
//  def children: List[Child[P]] = ???
//  def parentMethod(): Unit = println("parent")
//}
//
//trait Child[P <: Parent[P]] {
//  def parent: P = ???
//  def doParentThings(): Unit = parent.parentMethod()
//}
//
//trait SubParent[P <: SubParent[P]] extends Parent[SubParent[P]] {
//  def subParentMethod(): Unit = println("subparent")
//}
//
//trait SubChild[P <: SubParent[P]] extends Child[SubParent[P]] {
//  def subParent: P = ???
//  def doSubParentThings(): Unit = subParent.subParentMethod()
//}
//
//trait SubSubParent[P <: SubSubParent[P]] extends SubParent[SubSubParent[P]] {
//  def subSubParentMethod(): Unit = println("subsubparent")
//  def x: Unit = {
//    val c = children
//  }
//}
//
//trait SubSubChild[P <: SubSubParent[P]] extends SubChild[SubSubParent[P]] {
//  def subSubParent: P = ???
//  def doSubSubParentThings(): Unit = subSubParent.subSubParentMethod()
//  def doThings(): Unit = {
//    doParentThings()
//    doSubSubParentThings()
//    doSubSubParentThings()
//  }
//}

trait DelegatedInteractor[InteractorType <: DelegatingInteractor[InteractorType]] extends Interactor {
  def parent: DelegatingInteractor[InteractorType]
}

object DelegatingInteractor {

  import scala.language.implicitConversions

  implicit def asInteractorType[InteractorType <: DelegatingInteractor[InteractorType]](
    interactor: DelegatingInteractor[InteractorType]
  ): InteractorType = {
    interactor.asInstanceOf[InteractorType]
  }
}

trait DelegatingInteractor[InteractorType <: DelegatingInteractor[InteractorType]] extends Interactor {
  self =>
  def frame: ScalismoFrame

  private var _delegate: DelegatedInteractor[InteractorType] = initialDelegate

  protected def initialDelegate: DelegatedInteractor[InteractorType]

  // honestly, I've had enough of all the type fiddling. If somebody requests it as a subtype, they'll know it's correctly typed.
  protected def delegate[D <: DelegatedInteractor[InteractorType]]: D = _delegate.asInstanceOf[D]

  protected def delegate_=(newDelegate: DelegatedInteractor[InteractorType]): Unit = {
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
