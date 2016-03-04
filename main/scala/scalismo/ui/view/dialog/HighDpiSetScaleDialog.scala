package scalismo.ui.view.dialog

import java.awt.Font
import java.awt.event.{ KeyAdapter, KeyEvent, MouseAdapter, MouseEvent }
import javax.swing.UIManager

import scalismo.ui.view.{ FancySlider, HighDpi, ScalismoFrame }

import scala.swing._
import scala.swing.event.ValueChanged

class HighDpiSetScaleDialog(implicit val frame: ScalismoFrame) extends Dialog(frame) {

  title = "DPI scaling"

  val reset = new Button(new Action("Reset") {
    override def apply(): Unit = {
      scaleSlider.value = initialScale
      pack()
    }
  })

  val cancel = new Button(new Action("Cancel") {
    override def apply(): Unit = {
      dispose()
    }
  })

  val ok = new Button(new Action("OK") {
    override def apply(): Unit = {
      val factor = scaleSlider.value / 100f

      if (factor != HighDpi.scaleFactor) {
        HighDpi.scaleFactor = factor
        Dialog.showMessage(main, "Please restart the application to see your changes applied.", "UI scale was changed")
      }
      dispose()
    }
  })

  val initialScale = Math.round(HighDpi.scaleFactor * 100)
  val label = new Label("This is an example of how the UI would look at the chosen setting.")
  val main = new BorderPanel

  val scaleSlider = new FancySlider {
    min = 25
    max = 400
    value = initialScale

    override def formattedValue(sliderValue: Int): String = s"$sliderValue%"

    def sliderLabels: List[Component] = List(minLabel, maxLabel, valueLabel)
  }

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
    val unscaledIcon = UIManager.getIcon("OptionPane.informationIcon")
    val scaleFactor: Float = scaleSlider.value / 100.0f
    val defaultIconPixels = 14

    val scaledWidth = Math.round(scaleFactor * defaultIconPixels)
    val scaledHeight = Math.round(scaleFactor * defaultIconPixels)
    val scaledIcon = HighDpi.scaleIcon(unscaledIcon, scaledWidth, scaledHeight)
    label.icon = scaledIcon

    // 12 is the default font size
    val textSize = Math.round(12 * scaleFactor)

    (List(label, reset, cancel, ok) ++ scaleSlider.sliderLabels).foreach { component =>
      component.font = new Font(component.font.getName, component.font.getStyle, textSize)
    }
  }

  updateLayout()
  listenTo(scaleSlider)

  reactions += {
    case ValueChanged(_) => updateLayout()
  }

  main.layout(scaleSlider) = BorderPanel.Position.North
  main.layout(label) = BorderPanel.Position.Center
  main.layout(new GridPanel(1, 3) {
    contents ++= Seq(reset, cancel, ok)
  }) = BorderPanel.Position.South

  contents = main
  modal = true
  pack()
  centerOnScreen()
}
