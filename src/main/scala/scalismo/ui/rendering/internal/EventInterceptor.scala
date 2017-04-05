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

package scalismo.ui.rendering.internal

import java.awt.event.{ KeyEvent, MouseEvent, MouseWheelEvent, MouseWheelListener }

import scalismo.ui.control.interactor.Interactor.Verdict.Block
import scalismo.ui.view.ScalismoFrame
import vtk.rendering.vtkEventInterceptor

class EventInterceptor(frame: ScalismoFrame) extends vtkEventInterceptor with MouseWheelListener {

  override def keyPressed(e: KeyEvent): Boolean = frame.interactor.keyPressed(e) == Block

  override def keyTyped(e: KeyEvent): Boolean = frame.interactor.keyTyped(e) == Block

  override def mouseMoved(e: MouseEvent): Boolean = frame.interactor.mouseMoved(e) == Block

  override def mouseExited(e: MouseEvent): Boolean = frame.interactor.mouseExited(e) == Block

  override def mouseClicked(e: MouseEvent): Boolean = frame.interactor.mouseClicked(e) == Block

  override def keyReleased(e: KeyEvent): Boolean = frame.interactor.keyReleased(e) == Block

  override def mouseDragged(e: MouseEvent): Boolean = frame.interactor.mouseDragged(e) == Block

  override def mouseEntered(e: MouseEvent): Boolean = frame.interactor.mouseEntered(e) == Block

  override def mousePressed(e: MouseEvent): Boolean = frame.interactor.mousePressed(e) == Block

  override def mouseReleased(e: MouseEvent): Boolean = frame.interactor.mouseReleased(e) == Block

  // This is an extension to the original vtkEventInterceptor.
  // Since VTK doesn't handle mouse wheel events anyway, there's no need to block them, hence a Unit return type.
  override def mouseWheelMoved(e: MouseWheelEvent): Unit = frame.interactor.mouseWheelMoved(e)
}
