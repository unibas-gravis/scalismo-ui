package scalismo.ui.swing.util

import java.awt.Color

import scalismo.ui.Axis

object AxisColor {
  def forAxis(axis: Axis.Value, darker: Boolean = false): Color = {
    val color = axis match {
      case Axis.X => Color.RED
      case Axis.Y => Color.GREEN
      case Axis.Z => Color.BLUE
    }
    if (darker) color.darker() else color
  }
}
