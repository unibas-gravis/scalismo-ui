package scalismo.ui.view

import java.awt.{ Font, GraphicsEnvironment, Transparency }
import javax.swing.plaf.FontUIResource
import javax.swing.{ Icon, ImageIcon, UIDefaults, UIManager }

import scalismo.ui.settings.GlobalSettings

import scala.collection.mutable
import scala.util.Try

object HighDpi {

  private var _factor: Float = GlobalSettings.get[Float](GlobalSettings.Keys.HighDpiScale).getOrElse(1)

  def scaleFactor: Float = _factor

  def scaleFactor_=(newValue: Float): Unit = {
    GlobalSettings.set[Float](GlobalSettings.Keys.HighDpiScale, newValue)
    _factor = newValue
  }

  def scale(loDpiPixels: Int) = Math.round(loDpiPixels * scaleFactor)

  private def transparentIcon(width: Int, height: Int): ImageIcon = Try {
    val ge = GraphicsEnvironment.getLocalGraphicsEnvironment
    val gd = ge.getDefaultScreenDevice
    val gc = gd.getDefaultConfiguration
    val image = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT)
    new ImageIcon(image)
  }.getOrElse(null)

  def scaleIcon(sourceIcon: Icon, width: Int, height: Int): Icon = {
    sourceIcon match {
      case icon: ImageIcon => new ImageIcon(icon.getImage.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH))
      case icon: Icon => Try {
        val (w, h) = (icon.getIconWidth, icon.getIconHeight)
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment
        val gd = ge.getDefaultScreenDevice
        val gc = gd.getDefaultConfiguration
        val image = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT)
        val g = image.createGraphics()
        icon.paintIcon(null, g, 0, 0)
        g.dispose()
        new ImageIcon(image.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH))
      }.getOrElse(transparentIcon(width, height))
      case _ => transparentIcon(width, height)
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
