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

package scalismo.ui.api

import scalismo.ui.control.interactor.{ DefaultInteractor, Interactor }
import scalismo.ui.util.EdtUtil
import scalismo.ui.view.{ ScalismoFrame, ScalismoLookAndFeel }

class ScalismoUI(title: String) extends SimpleAPI {

  private[ui] val fr = EdtUtil.onEdtWait {
    val frame = new ScalismoFrame()

    frame.setup(Array[String]())
    frame.pack()
    frame.visible = true

    frame.title = title;
    frame
  }

  override protected[api] val scene = fr.scene
  override protected[api] val frame = fr
}

object ScalismoUI {

  def apply(title: String = ""): ScalismoUI = {
    scalismo.initialize()
    ScalismoLookAndFeel.initializeWith(ScalismoLookAndFeel.DefaultLookAndFeelClassName)
    new ScalismoUI(title)
  }
}

