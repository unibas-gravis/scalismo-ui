package scalismo.ui.view

import javax.swing.BorderFactory
import javax.swing.border.TitledBorder

import scalismo.ui.model.Axis

import scala.swing.{ Label, Action, Orientation, BorderPanel }

abstract class ViewportPanel(val frame: ScalismoFrame) extends BorderPanel {
  def name: String

  val toolBar = new ToolBar {
    floatable = false
    rollover = true
    orientation = Orientation.Horizontal
  }

  def setupToolBar(): Unit = {
    toolBar.add(new Action("SS") {
      override def apply(): Unit = ??? //new SaveAction(screenshot, PngFileIoMetadata).apply()
    }).tooltip = "Screenshot"

    toolBar.add(new Action("RC") {
      override def apply(): Unit = ??? //resetCamera()
    }).tooltip = "Reset Camera"
  }

  def setupLayout(): Unit = {
    layout(toolBar) = BorderPanel.Position.North
    layout(new Label("dummy")) = BorderPanel.Position.Center
  }

  border = new TitledBorder(name)

  // constructor
  setupToolBar()
  setupLayout()
}

class ViewportPanel2D(frame: ScalismoFrame, val axis: Axis) extends ViewportPanel(frame) {
  override def name = axis.toString

  border match {
    case titled: TitledBorder => titled.setTitleColor(AxisColor.forAxis(axis).darker())
    case _ => // unexpected, can't handle
  }
}

class ViewportPanel3D(frame: ScalismoFrame, override val name: String = "3D") extends ViewportPanel(frame) {

}