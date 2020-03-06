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

import java.awt.Color
import java.awt.image.BufferedImage

import javax.swing._
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.util.ColorChooserPanel
import scalismo.ui.view.util.ScalableUI.implicits._

import scala.swing.{Action, Alignment, BorderPanel, Button, Dialog, FlowPanel, GridPanel, Label, TextField}

class BackgroundColorDialog(implicit val frame: ScalismoFrame) extends Dialog(frame) {

  private val colorChooser = new ColorChooserPanel

  private val presetsPanel = {
    val pixels = 32.scaled

    val presets: List[(Color, String)] = List((Color.WHITE, "White"),
                                              (new Color(230, 230, 230), "Gray 10%"),
                                              (new Color(192, 192, 192), "Gray 25%"),
                                              (new Color(128, 128, 128), "Gray 50%"),
                                              (Color.BLACK, "Black"))

    new GridPanel(presets.length, 1) {
      border = BorderFactory.createTitledBorder("Presets")
      presets.foreach {
        case (color, description) =>
          val image = new BufferedImage(pixels, pixels, BufferedImage.TYPE_INT_RGB)
          val g = image.createGraphics()
          g.setColor(color)
          g.fillRect(0, 0, pixels, pixels)
          g.dispose()

          val icon = new ImageIcon(image)
          val button = new Button(new Action(description) {
            override def apply(): Unit = {
              colorChooser.color = color
            }
          })
          button.icon = icon
          button.horizontalAlignment = Alignment.Left
          button.iconTextGap = 12.scaled
          contents += button
      }
    }
  }

  private val customPanel = new BorderPanel {
    border = BorderFactory.createTitledBorder("Custom")

    layout(colorChooser) = BorderPanel.Position.Center
  }

  private val closeButton = new Button(new Action("Close") {
    override def apply(): Unit = dispose()
  })

  private val hexField = new TextField(6)
  private val setButton = new Button(new Action("Set") {
    override def apply(): Unit = {
      val input = hexField.text
      val check = input.toLowerCase
      if (check.matches("^[0-9a-f]{6}$")) {
        val hex = Integer.parseInt(check, 16)
        colorChooser.color = new Color(hex)
      } else {
        JOptionPane.showMessageDialog(BackgroundColorDialog.this.peer,
                                      "Color codes must be exactly 6 hex digits.",
                                      "Invalid input",
                                      JOptionPane.ERROR_MESSAGE)
      }
    }
  })

  private val southPanel: BorderPanel = new BorderPanel {
    val manualPanel = new FlowPanel(new Label("Hex:"), hexField, setButton)
    layout(closeButton) = BorderPanel.Position.West
    layout(manualPanel) = BorderPanel.Position.East
  }

  private val mainPanel: BorderPanel = new BorderPanel() {

    layout(presetsPanel) = BorderPanel.Position.West
    layout(customPanel) = BorderPanel.Position.Center
    layout(southPanel) = BorderPanel.Position.South

    val b: Int = 5.scaled
    border = BorderFactory.createEmptyBorder(b, b, b, b)
  }

  listenTo(colorChooser)

  reactions += {
    case ColorChooserPanel.event.ColorChanged(source) if source == colorChooser =>
      setBackgroundColor(colorChooser.color)
  }

  // This method is only invoked in reaction to events from the color chooser.
  // All other ways of setting a background color go through the chooser,
  // thus ensuring a consistent view.
  private def setBackgroundColor(color: Color): Unit = {
    // also update the text field before actually changing the color
    val hexCode = Integer.toHexString(color.getRGB & 0xFFFFFF).toUpperCase
    val prepend = "0" * (6 - hexCode.length)
    hexField.text = prepend + hexCode

    frame.sceneControl.backgroundColor.value = color
  }

  // constructor

  colorChooser.color = frame.sceneControl.backgroundColor.value

  contents = mainPanel
  title = "Set Background Color"
  modal = true
  pack()
  centerOnScreen()

}
