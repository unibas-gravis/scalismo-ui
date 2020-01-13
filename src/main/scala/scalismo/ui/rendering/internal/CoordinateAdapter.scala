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

import java.awt.{Component, Point}

import scalismo.ui.rendering.internal.CoordinateAdapter.VtkPoint

object CoordinateAdapter {

  class VtkPoint(x: Int, y: Int) extends Point(x, y)

}

/**
 * This class converts 2D coordinates, as used by
 * AWT, to 2D coordinates usable in VTK.
 *
 * In a nutshell, VTK uses "zero-at-bottom" y coordinates,
 * whereas AWT uses "zero-at-top" ones.
 *
 * In addition, VTK and AWT may disagree on how large
 * a pixel even is. This has been observed on MacBooks with
 * high-resolution Retina displays, where a "Java pixel" is in
 * fact 2x2 physical pixels. VTK works with the physical
 * pixels, whereas our events contain "Java pixels".
 *
 * This class detects these cases and automatically applies
 * scaling.
 *
 * Note: Because performance is important at this low implementation level,
 * we treat the various possible cases separately.
 *
 */
class CoordinateAdapter {
  // native window height (real pixels). This should always be a valid value when retrieved in toVtkPoint().
  private var nativeHeight: Int = 0

  // (fast) integer scaling factor. If not 0, it is applied both vertically and horizontally. Should apply
  // in almost all cases, and have the value 1 or 2, except (possibly) for new Macintosh Retina models.
  // This value is re-evaluated (i.e., set) whenever the setSize() method is called. If it is found to not
  // be an integer value > 0, then it is set to 0 to trigger usage of the more complicated methods mentioned below.
  private var intScale: Int = 1

  // custom scaling factor, might be different for x and y dimensions. Applies to new MacBook models.
  // see: https://www.heise.de/newsticker/meldung/Neues-MacBook-Pro-Standardaufloesung-ist-skaliert-3559305.html (german)
  private var xScale: Float = 1
  private var yScale: Float = 1

  /**
   * Set the size of the panel in which coordinates are to be
   * transformed. This will store the height (so that y
   * coordinates can be converted), and calculate the scale.
   * The only scale values that have been observed in the wild
   * so far are 1 and 2.
   *
   * @param width  width as reported by JOGL panel
   * @param height height as reported by JOGL panel
   * @param panel  the JOGL panel itself, used to determine
   *               what Java thinks the width and height are.
   *
   */
  def setSize(width: Int, height: Int, panel: Component): Unit = {
    this.nativeHeight = height

    val panelSize = panel.getSize
    val panelHeight = panelSize.getHeight
    val panelWidth = panelSize.getWidth

    // bail out if anything is abnormal (division by zero...)
    if (panelHeight <= 0 || panelWidth <= 0) {
      intScale = 1
      return
    }

    // calculate (double) scale factors
    val dxScale = width.toDouble / panelWidth
    val dyScale = height.toDouble / panelHeight

    val ixScale = dxScale.toInt
    val iyScale = dyScale.toInt

    // determine if we're in a "simple scale" setting, where x and y are integers and the same
    if (ixScale == iyScale && ixScale * panelWidth == width && iyScale * panelHeight == height) {
      intScale = ixScale
    } else {
      intScale = 0
      xScale = dxScale.toFloat
      yScale = dyScale.toFloat
    }
  }

  def toVtkPoint(awtPoint: Point): VtkPoint = {
    if (intScale != 0) {
      // Simple case, just scale by an integer (usually 1 or 2)
      val x = awtPoint.x * intScale
      val y = nativeHeight - (awtPoint.y * intScale) - 1
      new VtkPoint(x, y)
    } else {
      // Complex case, scale by an arbitrary number.
      // As using rounding operations (Math.round) is a *massive* performance penalty
      // (at *least* a 10x factor, usually more like 30), we'll live with a 1px inaccuracy.
      val x = (awtPoint.x * xScale).toInt
      val y = nativeHeight - (awtPoint.y * yScale).toInt - 1
      new VtkPoint(x, y)
    }
  }
}
