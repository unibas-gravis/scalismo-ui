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

package scalismo.ui.swing.util

import java.awt.{ Dimension, Font, GraphicsEnvironment, Transparency }
import javax.swing.plaf.FontUIResource
import javax.swing.{ Icon, ImageIcon, UIDefaults, UIManager }

import scalismo.ui.resources.icons.ScalableIcon
import scalismo.ui.settings.PersistentSettings

import scala.collection.mutable

object ScalableUI {

  class ScalableInt(val self: Int) extends AnyVal {
    def scaled: Int = scale(self)
  }

  object implicits {

    import scala.language.implicitConversions

    implicit def scalableInt(int: Int): ScalableInt = new ScalableInt(int)
  }

  private var _factor: Float = PersistentSettings.get[Float](PersistentSettings.Keys.HighDpiScale).getOrElse(1)

  def scaleFactor: Float = _factor

  def scaleFactor_=(newValue: Float): Unit = {
    PersistentSettings.set[Float](PersistentSettings.Keys.HighDpiScale, newValue)
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

  def standardSizedIcon(icon: Icon): Icon = {
    icon match {
      case scalable: ScalableIcon => scalable.standardSized()
      case i =>
        val standardSize = ScalableUI.scale(Constants.StandardUnscaledIconSize)
        resizeIcon(i, standardSize, standardSize)
    }
  }

  def resizeIcon(sourceIcon: Icon, width: Int, height: Int): Icon = {
    if (sourceIcon.getIconWidth == width && sourceIcon.getIconHeight == height) {
      sourceIcon
    } else {
      sourceIcon match {
        case icon: ScalableIcon => icon.resize(width, height)
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
