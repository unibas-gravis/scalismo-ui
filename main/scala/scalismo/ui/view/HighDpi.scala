package scalismo.ui.view

import java.awt.{ Font, GraphicsEnvironment, Transparency }
import javax.swing.plaf.FontUIResource
import javax.swing.{ Icon, ImageIcon, UIDefaults, UIManager }

import scala.collection.mutable
import scala.util.Try

object HighDpi {

  // TODO: Whis will have to be configurable
  val zoomFactor: Float = 20.0f / 12.0f

  def scale(loDpiPixels: Int) = Math.round(loDpiPixels * zoomFactor)

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
