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

package scalismo.ui.view.dialog

import java.awt.Font
import java.awt.event.{ KeyAdapter, KeyEvent, MouseAdapter, MouseEvent }

import javax.swing.BorderFactory
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.util.ScalableUI.implicits.scalableInt
import scalismo.ui.view.util.{ Constants, FancySlider, ScalableUI }

import scala.swing._
import scala.swing.event.ValueChanged

class DisplayScalingDialog(implicit val frame: ScalismoFrame) extends Dialog(frame) {

  title = "Display Scaling"

  val cancel = new Button(new Action("Cancel") {
    override def apply(): Unit = {
      dispose()
    }
  })

  val ok = new Button(new Action("OK") {
    override def apply(): Unit = {
      val factor = scaleSlider.value / 100f

      if (factor != ScalableUI.scaleFactor) {
        ScalableUI.scaleFactor = factor
        Dialog.showMessage(frame.componentForDialogs, "Please restart the application to see your changes applied.", "Display Scaling was changed")
      }
      dispose()
    }
  })

  private val initialScale = Math.round(ScalableUI.scaleFactor * 100)

  val example: Label = new Label("This is an example of how the User Interface would look at the chosen setting.") {
    private val b = Constants.DefaultFontSize
    border = BorderFactory.createEmptyBorder(b, b, b, b)
  }

  val main: BorderPanel = new BorderPanel {
    private val b = 5.scaled
    border = BorderFactory.createEmptyBorder(b, b, b, b)
  }

  private class ScaleSlider extends FancySlider {
    min = 25
    max = 400
    value = initialScale

    override def formattedValue(sliderValue: Int): String = s"$sliderValue%"

    def sliderLabels: List[Component] = List(minLabel, maxLabel, valueLabel)
  }

  private val scaleSlider = new ScaleSlider()

  // when the user is done sliding, re-pack the window

  scaleSlider.peer.addMouseListener(new MouseAdapter {
    override def mouseReleased(e: MouseEvent): Unit = {
      pack()
    }
  })

  scaleSlider.peer.addKeyListener(new KeyAdapter {
    override def keyReleased(e: KeyEvent): Unit = {
      pack()
    }
  })

  def updateLayout(): Unit = {
    val unscaledIcon = BundledIcon.Information
    val scaleFactor: Float = scaleSlider.value / 100.0f

    val scaledWidth = Math.round(scaleFactor * Constants.StandardUnscaledIconSize)
    val scaledHeight = Math.round(scaleFactor * Constants.StandardUnscaledIconSize)
    val scaledIcon = ScalableUI.resizeIcon(unscaledIcon, scaledWidth, scaledHeight)
    example.icon = scaledIcon

    val textSize = Math.round(Constants.DefaultFontSize * scaleFactor)

    (List(example, cancel, ok) ++ scaleSlider.sliderLabels).foreach { component =>
      component.font = new Font(component.font.getName, component.font.getStyle, textSize)
    }
    // just cosmetical
    val b = Math.round(Constants.DefaultFontSize * scaleFactor)
    example.border = BorderFactory.createEmptyBorder(b, b, b, b)

  }

  updateLayout()
  listenTo(scaleSlider)

  reactions += {
    case ValueChanged(_) => updateLayout()
  }

  main.layout(scaleSlider) = BorderPanel.Position.North
  main.layout(example) = BorderPanel.Position.Center
  main.layout(new GridPanel(1, 2) {
    contents ++= Seq(cancel, ok)
  }) = BorderPanel.Position.South

  contents = main
  modal = true
  pack()
  centerOnScreen()
}
