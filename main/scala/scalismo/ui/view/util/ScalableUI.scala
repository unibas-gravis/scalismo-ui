package scalismo.ui.view.util

import java.awt.{ Dimension, Font, GraphicsEnvironment, Transparency }
import javax.swing.plaf.FontUIResource
import javax.swing.{ Icon, ImageIcon, UIDefaults, UIManager }

import scalismo.ui.resources.icons.FontIcon
import scalismo.ui.settings.GlobalSettings

import scala.collection.mutable

object ScalableUI {

  class ScalableInt(val self: Int) extends AnyVal {
    def scaled: Int = scale(self)
  }

  object implicits {

    import scala.language.implicitConversions

    implicit def scalableInt(int: Int): ScalableInt = new ScalableInt(int)
  }

  private var _factor: Float = GlobalSettings.get[Float](GlobalSettings.Keys.HighDpiScale).getOrElse(1)

  def scaleFactor: Float = _factor

  def scaleFactor_=(newValue: Float): Unit = {
    GlobalSettings.set[Float](GlobalSettings.Keys.HighDpiScale, newValue)
    _factor = newValue
  }

  /**
   * Scale (multiply) a given number by a factor.
   *
   * The first argument is usually a pixel count.
   * The second argument is a multiplier, and is usually left unspecified, so that
   * the default value ([[ScalableUI.scaleFactor]] is used.
   *
   * @param loDpiPixels unscaled number (usually in pixels)
   * @param factor      scale factor. If not specified, [[ScalableUI.scaleFactor]] is used.
   * @return the argument multiplied by the given factor, rounded to the next integer.
   */
  def scale(loDpiPixels: Int, factor: Float = scaleFactor): Int = Math.round(loDpiPixels * factor)

  def scaleDimension(loDpiDimension: Dimension, factor: Float = scaleFactor): Dimension = {
    new Dimension(scale(loDpiDimension.width, factor), scale(loDpiDimension.height, factor))
  }

  /**
   * Scales an icon by the given factor.
   * The resulting icon's size will be the size of the icon, multiplied by the given factor.
   *
   * @param sourceIcon the icon to scale
   * @param factor     the factor to change the icon's size. If unspecified, the currently set default scale factor is applied
   * @return the scaled icon
   */
  def scaleIcon(sourceIcon: Icon, factor: Float = scaleFactor): Icon = {
    resizeIcon(sourceIcon, scale(sourceIcon.getIconWidth), scale(sourceIcon.getIconHeight))
  }

  def resizeIcon(sourceIcon: Icon, width: Int, height: Int): Icon = {
    if (sourceIcon.getIconWidth == width && sourceIcon.getIconHeight == height) {
      sourceIcon
    } else {
      sourceIcon match {
        case icon: FontIcon => icon.resize(width, height)
        case icon: ImageIcon => new ImageIcon(icon.getImage.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH))
        case icon: Icon =>
          val (w, h) = (icon.getIconWidth, icon.getIconHeight)
          val ge = GraphicsEnvironment.getLocalGraphicsEnvironment
          val gd = ge.getDefaultScreenDevice
          val gc = gd.getDefaultConfiguration
          val image = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT)
          val g = image.createGraphics()
          icon.paintIcon(null, g, 0, 0)
          g.dispose()
          new ImageIcon(image.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH))
      }
    }
  }

  def updateLookAndFeelDefaults(): Unit = {
    updateDefaults(UIManager.getDefaults)
    updateDefaults(UIManager.getLookAndFeelDefaults)
  }

  private def updateDefaults(defaults: UIDefaults): Unit = {
    val keys = defaults.keys()
    val replacements = new mutable.LinkedHashMap[String, AnyRef]

    while (keys.hasMoreElements) {
      val key = keys.nextElement()
      key match {
        case stringKey: String if stringKey.toLowerCase.endsWith("font") =>
          val value = defaults.get(key)
          value match {
            case fr: FontUIResource =>
              val size = scale(fr.getSize)
              replacements(stringKey) = new FontUIResource(fr.getName, fr.getStyle, size)
            case f: Font =>
              val size = scale(f.getSize)
              replacements(stringKey) = new Font(f.getName, f.getStyle, size)
            case _ => // do nothing
          }
        case _ => // nothing
      }
    }

    replacements.foreach {
      case (key, value) =>
        defaults.put(key, value)
    }
  }
}
