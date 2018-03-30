package scalismo.ui.view.dialog

import java.awt.event.{MouseAdapter, MouseEvent}
import java.awt.image.BufferedImage
import java.awt.{Color, Dimension, Graphics}

import javax.swing._
import javax.swing.event.{ChangeEvent, ChangeListener}
import scalismo.ui.event.{Event, ScalismoPublisher}
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.swing.ColorPickerPanel
import scalismo.ui.view.util.ScalableUI.implicits._

import scala.swing.{Action, Alignment, BorderPanel, Button, Component, Dialog, FlowPanel, GridPanel, Label, TextField}


class BackgroundColorDialog(implicit val frame: ScalismoFrame) extends Dialog(frame) {

  private case class ColorSelectedInWheel(color: Color) extends Event

  private case class ColorSelected(color: Color) extends Event


  class BrightnessSelector extends JComponent with ScalismoPublisher {
    private val BarWidth = 20.scaled
    private val MarkerWidth = 10.scaled
    // total width will be MarkerWidth + BarWidth

    // half of the marker height. Total marker height should be this * 2 + 1
    private val MarkerHeightHalf = 1.scaled


    private var baseColor: Color = Color.WHITE

    private var brightness: Float = 1.0f

    private def setBrightness(b: Float): Unit = {
      // sanity check
      if (b >= 0.0 && b <= 1.0) {
        brightness = b
        updateUiAndTriggerEvent()
      }
    }

    def setBaseColor(bc: Color): Unit = {
      baseColor = bc
      updateUiAndTriggerEvent()
    }

    def updateUiAndTriggerEvent(): Unit = {
      invalidate()
      repaint()
      val hsb = Color.RGBtoHSB(baseColor.getRed, baseColor.getGreen, baseColor.getBlue, null)
      val color = new Color(Color.HSBtoRGB(hsb(0), hsb(1), brightness))
      publishEvent(ColorSelected(color))
    }

    // if we don't define both of these, then strange things happen.
    override def getMinimumSize: Dimension = new Dimension(MarkerWidth + BarWidth, 0)

    override def getPreferredSize: Dimension = getMinimumSize

    override def paint(g: Graphics): Unit = {
      // we're always assuming a brightness of 1.0 of the base color
      val hsb = Color.RGBtoHSB(baseColor.getRed, baseColor.getGreen, baseColor.getBlue, null)
      val hue = hsb(0)
      val saturation = hsb(1)


      val dim = getSize(null)

      val width = Math.max(dim.width, BarWidth) - MarkerWidth
      val height = dim.height

      // for every vertical pixel that we have...
      (0 until dim.height).foreach { h =>
        // determine its relative brightness...
        val brightness = 1.0f - h.toFloat / height
        // translate that to a color...
        val rgb = Color.HSBtoRGB(hue, saturation, brightness)
        g.setColor(new Color(rgb))
        // and to coordinates
        g.fillRect(MarkerWidth, h, width, 1)
      }

      // and, now, paint the marker. We're not smart or fancy here, just functional.
      g.setColor(Color.BLACK)
      val markerCenter = {
        val offsetRelative = 1.0f - brightness
        val offsetAbsolute = offsetRelative * height
        offsetAbsolute.toInt
      }
      g.fillRect(0, markerCenter - MarkerHeightHalf, MarkerWidth, MarkerHeightHalf * 2 + 1)

    }

    def mouseClicked(e: MouseEvent): Unit = {
      if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount == 1) {
        val height = getSize(null).height
        val y = height - e.getY
        setBrightness(y.toFloat / height)
      }
    }
  }


  class ColorChooser extends Component with ChangeListener with ScalismoPublisher {
    override lazy val peer = new ColorPickerPanel()
    peer.setRGB(255, 255, 255)
    peer.addChangeListener(this)

    override def stateChanged(event: ChangeEvent): Unit = {
      val rgb = peer.getRGB
      val c: Color = new Color(rgb(0), rgb(1), rgb(2))
      publishEvent(ColorSelectedInWheel(c))
    }

    border = new javax.swing.border.EmptyBorder(10, 0, 0, 0)
  }


  private val presetsPanel = {
    val pixels = 32.scaled

    val predefined: List[(Color, String)] = List(
      (Color.WHITE, "White"),
      (new Color(230, 230, 230), "Gray 10%"),
      (new Color(192, 192, 192), "Gray 25%"),
      (new Color(128, 128, 128), "Gray 50%"),
      (Color.BLACK, "Black")
    )

    new GridPanel(predefined.length, 1) {
      border = BorderFactory.createTitledBorder("Presets")
      predefined.foreach { case (color, description) =>
        val image = new BufferedImage(pixels, pixels, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics()
        g.setColor(color)
        g.fillRect(0, 0, pixels, pixels)
        g.dispose()

        val icon = new ImageIcon(image)
        val button = new Button(new Action(description) {
          override def apply(): Unit = setBackgroundColor(color)
        })
        button.icon = icon
        button.horizontalAlignment = Alignment.Left
        button.iconTextGap = 12.scaled
        contents += button
      }
    }
  }

  def setBackgroundColor(color: Color): Unit = {
    // also update the text field before actually changing the color
    val hexCode = Integer.toHexString(color.getRGB & 0xFFFFFF).toUpperCase
    val prepend = "0" * (6 - hexCode.length)
    hexField.text = prepend + hexCode

    frame.sceneControl.backgroundColor.value = color
  }

  val colorWheel = new ColorChooser
  val brightness = new BrightnessSelector
  // small hack because we can't make BrightnessSelector extend two classes at the same time
  brightness.addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent): Unit = brightness.mouseClicked(e)
  })

  private val customPanel = new BorderPanel {
    border = BorderFactory.createTitledBorder("Custom")

    layout(colorWheel) = BorderPanel.Position.Center
    layout(Component.wrap(brightness)) = BorderPanel.Position.East
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
        val color = new Color(hex)
        frame.sceneControl.backgroundColor.value = color
      } else {
        JOptionPane.showMessageDialog(BackgroundColorDialog.this.peer, "Color codes must be exactly 6 hex digits.", "Invalid input", JOptionPane.ERROR_MESSAGE)
      }
    }
  })

  private val southPanel = new BorderPanel {
    val manualPanel = new FlowPanel(new Label("Hex:"), hexField, setButton)
    layout(closeButton) = BorderPanel.Position.West
    layout(manualPanel) = BorderPanel.Position.East
  }

  private val mainPanel = new BorderPanel() {

    layout(presetsPanel) = BorderPanel.Position.West
    layout(customPanel) = BorderPanel.Position.Center
    layout(southPanel) = BorderPanel.Position.South

    val b: Int = 5.scaled
    border = BorderFactory.createEmptyBorder(b,b,b,b)
  }

  listenTo(colorWheel, brightness)

  reactions += {
    case ColorSelectedInWheel(c) => brightness.setBaseColor(c)
    case ColorSelected(color) => setBackgroundColor(color)
  }

  contents = mainPanel
  title = "Set Background Color"
  modal = true
  pack()
  centerOnScreen()

}
