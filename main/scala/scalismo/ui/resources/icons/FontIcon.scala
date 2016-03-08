package scalismo.ui.resources.icons

import java.awt._
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import javax.swing.ImageIcon

import jiconfont.icons.FontAwesome
import jiconfont.{ IconCode, IconFont }
import scalismo.ui.view.Constants

object FontIcon {

  private val fonts: Map[String, Font] = {
    def create(iconFont: IconFont): (String, Font) = {
      (iconFont.getFontFamily, Font.createFont(Font.TRUETYPE_FONT, iconFont.getFontInputStream))
    }
    Seq(create(FontAwesome.getIconFont)).toMap
  }

  def load(code: IconCode, width: Int = Constants.StandardUnscaledIconSize, height: Int = Constants.StandardUnscaledIconSize, color: Color = Color.BLACK): FontIcon = {
    val string = Character.toString(code.getUnicode)
    val image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val graphics: Graphics2D = image.createGraphics()

    val baseFont = fonts(code.getFontFamily)
    val needed = new Dimension

    var font = baseFont
    var bounds: Rectangle2D = null

    var actualSize: Float = height
    // loop until we find a font size where the character will fit completely into the requested size
    do {
      font = baseFont.deriveFont(actualSize)
      val metrics = graphics.getFontMetrics(font)
      bounds = metrics.getStringBounds(string, graphics)
      needed.width = Math.ceil(bounds.getWidth).toInt
      needed.height = Math.ceil(bounds.getHeight).toInt
      actualSize -= .5f
    } while (needed.width > width || needed.height > height)

    // we might be smaller than requested (normally in at most one dimension), so adjust for that
    val xOffset: Float = (width - needed.width).toFloat / 2.0f
    val yOffset: Float = (height - needed.height).toFloat / 2.0f

    // now draw the text
    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
    graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
    graphics.setColor(color)
    graphics.setFont(font)
    // the y position also needs to take into account
    graphics.drawString(string, xOffset, yOffset + Math.abs(bounds.getY.toFloat))
    graphics.dispose()

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
