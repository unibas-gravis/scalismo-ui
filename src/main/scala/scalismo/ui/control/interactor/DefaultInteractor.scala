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

  override def mouseExited(e: MouseEvent): Verdict = {
    Recipe.ShiftKeySetsSlicePosition.mouseExited(e)
    Recipe.ControlKeyShowsImageInformation.mouseExited(e)
  }

  override def mouseWheelMoved(e: MouseWheelEvent): Verdict = {
    Recipe.Scroll2D.mouseWheelMoved(e)
  }
}
