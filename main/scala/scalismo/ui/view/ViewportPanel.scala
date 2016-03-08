package scalismo.ui.view

import java.awt.Color
import javax.swing.border.TitledBorder
import javax.swing.{ BorderFactory, SwingConstants }

import scalismo.ui.model.Axis
import scalismo.ui.resources.icons.BundledIcon

import scala.swing._

abstract class ViewportPanel(val frame: ScalismoFrame) extends BorderPanel {
  def name: String

  val renderer = new Label("RENDERER") {
    opaque = true
    background = Color.BLACK
    foreground = Color.YELLOW
  }

  val toolBar = new ToolBar {
    floatable = false
    rollover = true
    orientation = Orientation.Horizontal
  }

  def setupToolBar(): Unit = {
    toolBar.add(new Button() {
      tooltip = "Reset Camera"
      icon = BundledIcon.Reset.standardSized()
    })

    toolBar.add(new Button() {
      tooltip = "Screenshot"
      icon = BundledIcon.Screenshot.standardSized()
    })

  }

  def setupLayout(): Unit = {
    layout(toolBar) = BorderPanel.Position.North
    layout(renderer) = BorderPanel.Position.Center
  }

  border = new TitledBorder(name)

  // constructor
  setupToolBar()
  setupLayout()
}

class ViewportPanel3D(frame: ScalismoFrame, override val name: String = "3D") extends ViewportPanel(frame) {
  override def setupToolBar(): Unit = {
    super.setupToolBar()

    List(Axis.X, Axis.Y, Axis.Z).foreach { axis =>
      val button = new Button(axis.toString) {
        foreground = AxisColor.forAxis(axis).darker()
      }
      toolBar.add(button)
    }
  }
}

class ViewportPanel2D(frame: ScalismoFrame, val axis: Axis) extends ViewportPanel(frame) {
  override def name = axis.toString

  lazy val positionPlusButton = new Button("+")
  lazy val positionMinusButton = new Button("-")
  lazy val positionSlider = new Slider {
    peer.setOrientation(SwingConstants.VERTICAL)
  }

  lazy val sliderPanel = new BorderPanel {
    layout(positionPlusButton) = BorderPanel.Position.North
    layout(positionSlider) = BorderPanel.Position.Center
    layout(positionMinusButton) = BorderPanel.Position.South
  }

  override def setupLayout(): Unit = {
    super.setupLayout()
    layout(sliderPanel) = BorderPanel.Position.East
  }

  // constructor
  border match {
    case titled: TitledBorder => titled.setTitleColor(AxisColor.forAxis(axis).darker())
    case _ => // unexpected, can't handle
  }

  renderer.border = BorderFactory.createLineBorder(AxisColor.forAxis(axis), ScalableUI.scale(3))
}

