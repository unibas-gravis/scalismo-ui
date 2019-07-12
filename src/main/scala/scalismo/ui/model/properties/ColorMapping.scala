package scalismo.ui.model.properties

import java.awt.Color

import scalismo.color.RGB

/** Maps a range of values, determined by a lower and upper value to a color */
trait ColorMapping {

  var lowerColor: Color

  var upperColor: Color

  def mappingFunction(scalarRange: ScalarRange): (Double => Color)

  def suggestedNumberOfColors : Int
}

private[properties] case class LinearColorMapping(lColor : Color, uColor : Color) extends ColorMapping {

  override var lowerColor = lColor
  override var upperColor = uColor

  override def mappingFunction(scalarRange: ScalarRange): (Double => Color) = {
    value => {

      val lowerValue = scalarRange.cappedMinimum
      val upperValue = scalarRange.cappedMaximum
      if (value < lowerValue) lColor
      else if (value > upperValue) uColor
      else {
        // interpolating color
        val s = (value - lowerValue) / (upperValue - lowerValue)
        val newColor = (RGB(uColor) - RGB(lColor)) * s + RGB(lColor)
        newColor.toAWTColor
      }
    }
  }

  override val suggestedNumberOfColors = 100

}

object BlueToRedColorMapping extends LinearColorMapping(Color.BLUE, Color.GREEN) {
  override val suggestedNumberOfColors = 100
}

// TODO add a bunch of other color options
object WhiteToBlackMapping extends LinearColorMapping(Color.WHITE, Color.GRAY){
  override val suggestedNumberOfColors: Int = 100
}

object GreenToBlack extends LinearColorMapping(Color.GREEN, Color.GRAY){
  override val suggestedNumberOfColors: Int = 100
}
