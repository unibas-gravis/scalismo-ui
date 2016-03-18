package scalismo.ui.rendering.internal

import java.awt.{ Component, Point }

import scalismo.ui.rendering.internal.CoordinateAdapter.VtkPoint

object CoordinateAdapter {

  class VtkPoint(x: Int, y: Int) extends Point(x, y)

}

/**
 * This class converts 2D coordinates, as used by
 * AWT, to 2D coordinates useble in VTK.
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
 */
class CoordinateAdapter {
  var scale: Int = 1
  var height: Int = 0

  /**
   * Set the size of the panel in which coordinates are to be
   * transformed. This will store the height (so that y
   * coordinates can be converted), and calculate the scale.
   * The only scale values that have been observed in the wild
   * so far are 1 and 2.
   *
   * @param width width as reported by JOGL panel
   * @param height height as reported by JOGL panel
   * @param panel the JOGL panel itself, used to determine
   *              what Java thinks the width and height are.
   *
   */
  def setSize(width: Int, height: Int, panel: Component): Unit = {
    scale = height / panel.getSize.height
    this.height = height
  }

  def toVtkPoint(awtPoint: Point): VtkPoint = {
    val x = awtPoint.x * scale
    val y = height - (awtPoint.y * scale) - 1
    new VtkPoint(x, y)
  }
}
