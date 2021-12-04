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

package scalismo.ui.resources.icons

import java.awt._
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage

import javax.swing.ImageIcon
import jiconfont.icons.{Elusive, Entypo, FontAwesome}
import jiconfont.{IconCode, IconFont}
import scalismo.ui.view.util.Constants

object FontIcon {

  // this is a fully transparent white, so not a color we'll encounter.
  val RainbowColor: Color = new Color(0x00ffffff, true)

  // Map of supported icon fonts.
  private val fonts: Map[String, Font] = {
    def createTuple(iconFont: IconFont): (String, Font) = {
      (iconFont.getFontFamily, Font.createFont(Font.TRUETYPE_FONT, iconFont.getFontInputStream))
    }

    // this is where a new supported icon font would have to be added.
    val iconFonts = Seq(FontAwesome.getIconFont, Entypo.getIconFont, Elusive.getIconFont)
    iconFonts.map(createTuple).toMap
  }

  /**
   * can be used to generate a FontAwesome IconCode
   * that is not defined as a constant in the FontAwesome class.
   *
   * @param char a Unicode character, for example '\uf1e3'
   * @return an IconCode bound to the FontAwesome font, usable for the [[load]] method
   */
  def awesome(char: Char): IconCode = generic(char, FontAwesome.getIconFont)

  def generic(char: Char, iconFont: IconFont): IconCode = new IconCode {
    override def getUnicode: Char = char

    override def getFontFamily: String = iconFont.getFontFamily

    override def name(): String = "generated"
  }

  def load(code: IconCode,
           width: Int = Constants.StandardUnscaledIconSize,
           height: Int = Constants.StandardUnscaledIconSize,
           color: Color = Color.BLACK): FontIcon = {
    val string = Character.toString(code.getUnicode)
    val image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val graphics: Graphics2D = image.createGraphics()

    val baseFont = fonts(code.getFontFamily)

    val needed = new Dimension

    var font = baseFont
    var bounds: Rectangle2D = null

    var actualSize: Float = height
    // loop until we find a font size where the character will fit completely into the requested size
    var runLessThanOnce = true // we need to simulate a do while loop, which is not supported anymore in scala 3.0
    while (runLessThanOnce || needed.width > width || needed.height > height) {
      font = baseFont.deriveFont(actualSize)
      val metrics = graphics.getFontMetrics(font)
      bounds = metrics.getStringBounds(string, graphics)
      needed.width = Math.ceil(bounds.getWidth).toInt
      needed.height = Math.ceil(bounds.getHeight).toInt
      actualSize -= .5f
      runLessThanOnce = false
    }

    // we might be smaller than requested (normally in at most one dimension), so adjust for that
    val xOffset: Float = (width - needed.width).toFloat / 2.0f
    val yOffset: Float = (height - needed.height).toFloat / 2.0f

    // now draw the text
    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
    graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)

    graphics.setColor(if (color == RainbowColor) Color.BLACK else color)
    graphics.setFont(font)

    // the y position also needs to take into account how far the character goes below the baseline
    graphics.drawString(string, xOffset, yOffset + Math.abs(bounds.getY.toFloat))
    graphics.dispose()

    if (color == RainbowColor) {
      // adapted from: http://www.java2s.com/Code/Java/2D-Graphics-GUI/RainbowColor.htm
      def rainbow(x: Int, y: Int) = {
        val red = (x * 255) / (height - 1)
        val green = (y * 255) / (width - 1)
        // the higher the "blue" value, the brighter the overall image
        val blue = 160
        ((red << 16) | (green << 8) | blue) | 0xff000000
      }

      (0 until width).foreach { x =>
        (0 until height).foreach { y =>
          val alpha = (image.getRGB(x, y) >> 24) & 0xff
          // the higher the alpha threshold, the thinner the resulting icon will appear
          if (alpha > 128) {
            image.setRGB(x, y, rainbow(x, y))
          }
        }
      }
    }

    new FontIcon(code, color, image)
  }
}

class FontIcon(val code: IconCode, val color: Color, image: Image) extends ImageIcon(image) with ScalableIcon {
  override def resize(width: Int, height: Int): FontIcon = {
    FontIcon.load(code, width, height, color)
  }

  def colored(newColor: Color): FontIcon = {
    FontIcon.load(code, getIconWidth, getIconHeight, newColor)
  }
}
