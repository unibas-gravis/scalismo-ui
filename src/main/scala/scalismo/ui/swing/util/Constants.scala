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

package scalismo.ui.swing.util

import java.awt.Color

/**
 * The values in here are constants, in the sense that they're not supposed to change while an
 * application is running. However, they are still defined as vars, to allow developers to
 * easily override them if needed. If that is done, it should be done as early as possible
 * (e.g. in the main method), before an actual instance of the UI is created.
 */
object Constants {

  /**
   * This is the color value that is used as the basic background color when presenting color selections.
   * It is not totally in line with the actual background color used in the renderer (black) - however
   * setting this too dark, or too bright, will result in a perceived mismatch between the selected value
   * and what is actually seen on screen in the render window.
   *
   *
   */
  var PerceivedBackgroundColor: Color = Color.GRAY

  /**
   * This is the default size of icons, *before* any scaling is applied.
   */
  var StandardUnscaledIconSize: Int = 16

  /**
   * The default font size.
   * This seems to be baked in deeply in all of the Swing internals, so
   * DO NOT CHANGE THIS VALUE unless you REALLY know what you are doing.
   */
  var DefaultFontSize: Int = 12

}
