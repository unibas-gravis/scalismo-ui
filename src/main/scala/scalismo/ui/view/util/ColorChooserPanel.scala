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

package scalismo.ui.view.util

import java.awt._
import java.awt.event.MouseEvent
import java.awt.geom.AffineTransform

import javax.swing.event.{ ChangeEvent, ChangeListener, MouseInputAdapter }
import javax.swing.{ BorderFactory, JComponent }
import scalismo.ui.event.ScalismoPublisher
import scalismo.ui.view.swing.ColorPickerPanel
import scalismo.ui.view.util.ColorChooserPanel.event.ColorChanged
import scalismo.ui.view.util.ScalableUI.implicits._

import scala.swing.event.Event
import scala.swing.{ BorderPanel, Component, Graphics2D }

object ColorChooserPanel {

  object event {

    case class ColorChanged(source: ColorChooserPanel) extends Event

  }

}

class ColorChooserPanel extends BorderPanel with ChangeListener with ScalismoPublisher {

  private var _color = Color.WHITE

  private class WheelWrapper extends Component {
    override lazy val peer = new ColorPickerPanel()
  }

  class BrightnessSelector extends JComponent {

    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR))

    // default: white
    private var hsb: Array[Float] = Array(0f, 0f, 1.0f)

    private var brightness: Float = 1.0f

    private def setBrightness(b: Float): Unit = {
      brightness = Math.max(0.0f, Math.min(b, 1.0f))
      updateUiAndTriggerEvent()
    }

    def getBrightness: Float = brightness

    def setHSB(newHsb: Array[Float], ignoreBrightness: Boolean): Unit = {
      hsb = newHsb
      if (ignoreBrightness) {
        updateUiAndTriggerEvent()
      } else {
        // will also update the UI
        setBrightness(hsb(2))
      }
    }

    private def updateUiAndTriggerEvent(): Unit = {
      invalidate()
      repaint()
      // no need for listeners here, we directly call the enclosing instance's method
      ColorChooserPanel.this.stateChanged(new ChangeEvent(this))
    }

    private val mouseAdapter = new MouseInputAdapter() {
      override def mousePressed(e: MouseEvent): Unit = {
        setBrightness(pixelToBrightness(e.getY, getSize(null)))
      }

      override def mouseDragged(e: MouseEvent): Unit = {
        mousePressed(e)
      }
    }

    addMouseListener(mouseAdapter)
    addMouseMotionListener(mouseAdapter)

    // scale UI elements to at least be visible (1px)
    private def scale(px: Int, min: Int = 1): Int = {
      Math.max(1, px.scaled)
    }

    // width of the color bar
    private val barWidth = scale(20)
    // border around color bar
    private val borderPxXY = scale(1)
    // empty space outside of border
    private val outerPxXY = scale(2)
    // marker protrusion beyond border
    private val markerBeyondBorderPxX = scale(2)
    // half of the marker triangle edge length
    private val halfMarkerEdgeLength = scale(5, 2 * markerBeyondBorderPxX)

    // don't modify this, it ensures that triangles are actually drawn as triangles
    private val markerXPx = Math.round(Math.cos(Math.toRadians(30.0)) * (halfMarkerEdgeLength * 2).toDouble).toInt

    private val minTotalWidth = barWidth + (borderPxXY + markerBeyondBorderPxX + outerPxXY) * 2
    private val minTotalHeight = scale(60)

    // if we don't define both minimum and preferred size, then strange things happen.
    override def getMinimumSize: Dimension = new Dimension(minTotalWidth, minTotalHeight)

    override def getPreferredSize: Dimension = getMinimumSize

    override def paint(g: Graphics): Unit = {
      super.paint(g)

      val g2 = g.asInstanceOf[Graphics2D]
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

      // thicker lines for high-dpi displays, but at least 1.0
      g2.setStroke(new BasicStroke(Math.max(1.0f, ScalableUI.scaleFactor)))

      val size = getSize(null)

      paintBorder(g2, size)
      paintColors(g2, size)
      paintMarkers(g2, size)
    }

    // paint the currently available colors, from highest luminance (top) to lowest
    private def paintColors(g2: Graphics2D, size: Dimension): Unit = {
      val outsidePxX = outerPxXY + borderPxXY + markerBeyondBorderPxX
      val minX = outsidePxX
      val maxX = size.width - outsidePxX
      val xWidth = maxX - minX

      (0 until size.height).foreach { y =>
        val b = pixelToBrightness(y, size)
        if (b >= 0.0f && b <= 1.0f) {
          val rgb = Color.HSBtoRGB(hsb(0), hsb(1), b)
          g2.setColor(new Color(rgb))
          g2.fillRect(minX, y, xWidth, 1)
        }
      }
    }

    private def paintBorder(g2: Graphics2D, size: Dimension): Unit = {
      val outsidePxX = outerPxXY + markerBeyondBorderPxX
      val outsidePxY = outerPxXY + halfMarkerEdgeLength
      val minX = outsidePxX
      val maxX = size.width - outsidePxX
      val xWidth = maxX - minX

      g2.setColor(Color.DARK_GRAY)
      g2.fillRect(minX, outsidePxY, xWidth, size.height - outsidePxY * 2)
    }

    private def paintMarkers(g2: Graphics2D, size: Dimension): Unit = {
      // I initially wanted to draw markers on both sides of the bar,
      // but there's almost always a slight asymmetry.
      // If the markers aren't symmetric by even just a single pixel,
      // it looks disturbingly strange. So, there's only one marker now.

      //paintLeftMarker(brightness, g2, size)
      paintRightMarker(brightness, g2, size)
    }

    private def paintLeftMarker(b: Float, g2: Graphics2D, size: Dimension): Unit = {
      val innerY = brightnessToPixel(b, size)
      val innerX = markerXPx + outerPxXY

      val lowerX = innerX - markerXPx
      val lowerY = innerY + halfMarkerEdgeLength

      val upperX = lowerX
      val upperY = innerY - halfMarkerEdgeLength

      // draw triangle
      g2.setColor(Color.WHITE)
      g2.fillPolygon(Array(innerX, upperX, lowerX), Array(innerY, upperY, lowerY), 3)

      // draw outline
      g2.setColor(Color.BLACK)
      g2.drawPolygon(Array(innerX, upperX, lowerX), Array(innerY, upperY, lowerY), 3)

    }

    private def paintRightMarker(b: Float, g2: Graphics2D, size: Dimension): Unit = {
      // just flip the logic of painting the left marker using an affine transform on the graphics context

      val previousTransform = g2.getTransform

      val flip = AffineTransform.getScaleInstance(-1, 1)
      flip.translate(-size.width, 0)
      g2.transform(flip)

      paintLeftMarker(b, g2, size)

      g2.setTransform(previousTransform)
    }

    private def brightnessToPixel(b: Float, size: Dimension): Int = {
      val y = 1.0f - b
      val offsetY = halfMarkerEdgeLength + outerPxXY + borderPxXY
      val boxHeight = size.height - offsetY * 2
      val pxYrel = Math.round(y * boxHeight)
      val pxYabs = pxYrel + offsetY
      //println(s"b2px $b $pxYabs")
      //val b2 = pixelToBrightness(pxYabs, size)
      //println(s"px2b $pxYabs $b2")
      pxYabs
    }

    // Note: this method may return values outside of the allowable brightness range [0.0, 1.0];
    // This is intended behavior, as it allows to filter out pixels (positions) that don't
    // represent valid brightnesses (see usages of this method)
    private def pixelToBrightness(pxYAbs: Int, size: Dimension): Float = {
      val offsetY = halfMarkerEdgeLength + outerPxXY + borderPxXY
      val boxHeight = size.height - offsetY * 2
      val pxYrel = pxYAbs - offsetY
      val ratio = pxYrel.toFloat / boxHeight
      1.0f - ratio
    }

  }

  private class BrightnessWrapper extends BorderPanel {

    class Inner extends Component {
      override lazy val peer = new BrightnessSelector
    }

    val inner = new Inner

    layout(inner) = BorderPanel.Position.Center
    border = BorderFactory.createEmptyBorder(3.scaled, 0, 3.scaled, 0)
  }

  private val wheel = new WheelWrapper
  wheel.peer.addChangeListener(this)

  private val brightness = new BrightnessWrapper

  override def stateChanged(event: ChangeEvent): Unit = {
    if (event.getSource == wheel.peer) {
      // this will transitively cause another event from brightness
      brightness.inner.peer.setHSB(wheel.peer.getHSB, ignoreBrightness = true)
    } else if (event.getSource == brightness.inner.peer) {
      val hsb = wheel.peer.getHSB
      val rgb = Color.HSBtoRGB(hsb(0), hsb(1), brightness.inner.peer.getBrightness)
      // save and publish color
      _color = new Color(rgb)
      publishEvent(ColorChanged(this))
    }
  }

  def color: Color = _color

  // invoked externally; don't trigger any actions if the current color didn't change
  def color_=(newColor: Color): Unit = {
    if (newColor != _color) {
      setColor(newColor)
    }
  }

  // invoked internally; always trigger actions to update state
  private def setColor(newColor: Color): Unit = {
    _color = newColor
    val hsb = Color.RGBtoHSB(_color.getRed, _color.getGreen, _color.getBlue, null)
    wheel.peer.setHSB(hsb(0), hsb(1), 1.0f)
    brightness.inner.peer.setHSB(hsb, ignoreBrightness = false)
  }

  // constructor
  layout(wheel) = BorderPanel.Position.Center
  layout(brightness) = BorderPanel.Position.East

  // default color
  setColor(Color.WHITE)
}
