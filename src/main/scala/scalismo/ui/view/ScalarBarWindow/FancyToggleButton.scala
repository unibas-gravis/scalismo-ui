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

package scalismo.ui.view.ScalarBarWindow

import scalismo.ui.view.ScalismoFrame
import scala.swing.event.ButtonClicked
import scala.swing.ToggleButton

class FancyToggleButton (implicit frame:ScalismoFrame) extends ToggleButton {
  // I dont think I need to add anything
  def formattedValue(buttonValue: Int): String = buttonValue.toString

  // we now want to set the text when we click the button
  text = "Color Bar Off"
  opaque = true

  protected def updateText(): Unit = {
    if(opaque){
      text = "Color Bar On"
      opaque = false
      //new ScalarBar()

    }else{
      text = "Color Bar Off"
      opaque = true
    }
  }

  reactions += {
    case ButtonClicked(c) if c eq this => updateText()
  }


}
