package scalismo.ui.swing.props

import javax.swing.border.TitledBorder

import scalismo.ui.swing.util.FancySlider
import scalismo.ui.visualization.VisualizationProperty
import scalismo.ui.visualization.props.HasOpacity

import scala.swing.BorderPanel
import scala.swing.event.ValueChanged

class OpacityPropertyPanel extends BorderPanel with PropertyPanel {
  override def description: String = "Opacity"

  private var target: Option[HasOpacity] = None

  private val opacitySlider = new FancySlider {
    min = 0
    max = 100
    value = 100
  }

  {
    val northedPanel = new BorderPanel {
      val opacityPanel = new BorderPanel {
        border = new TitledBorder(null, description, TitledBorder.LEADING, 0, null, null)
        layout(opacitySlider) = BorderPanel.Position.Center
      }
      layout(opacityPanel) = BorderPanel.Position.Center
    }
    layout(northedPanel) = BorderPanel.Position.North
  }

  listenToOwnEvents()

  def listenToOwnEvents() = {
    listenTo(opacitySlider)
  }

  def deafToOwnEvents() = {
    deafTo(opacitySlider)
  }

  def updateUi() = {
    if (target.isDefined) {
      deafToOwnEvents()
      target.foreach(t => opacitySlider.value = (t.opacity.value * 100.0f).toInt)
      listenToOwnEvents()
    }
  }

  override def setObject(obj: Option[AnyRef]): Boolean = {
    cleanup()
    obj match {
      case Some(c: HasOpacity) =>
        target = Some(c)
        listenTo(c.opacity)
        updateUi()
        true
      case _ => false
    }
  }

  def cleanup(): Unit = {
    target.foreach(t => deafTo(t.opacity))
    target = None
  }

  reactions += {
    case VisualizationProperty.ValueChanged(_) => updateUi()
    case ValueChanged(c) => target.foreach(_.opacity.value = opacitySlider.value.toFloat / 100.0f)
  }

}
