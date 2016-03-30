package scalismo.ui.view.util

import java.awt.Color

import scalismo.ui.model.Axis

object AxisColor {
  def forAxis(axis: Axis): Color = {
    axis match {
      case Axis.X => Color.RED
      case Axis.Y => Color.GREEN
      case Axis.Z => Color.BLUE
    }
  }
}
