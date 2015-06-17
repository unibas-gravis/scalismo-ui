package scalismo.ui.swing.props

import javax.swing.border.TitledBorder

import scalismo.ui.swing.util.EdtSlider
import scalismo.ui.visualization.VisualizationProperty
import scalismo.ui.visualization.props.HasLineThickness

import scala.swing.BorderPanel
import scala.swing.event.ValueChanged

class LineThicknessPropertyPanel extends BorderPanel with PropertyPanel {
  override def description: String = "2D Outline Width"

  private var target: Option[HasLineThickness] = None

  private val slider = new EdtSlider {
    min = 1
    max = 8 // anything above 8 seems to be capped by VTK anyway
    value = min
  }

  {
    val northedPanel = new BorderPanel {
      val sliderPanel = new BorderPanel {
        border = new TitledBorder(null, description, TitledBorder.LEADING, 0, null, null)
        layout(slider) = BorderPanel.Position.Center
      }
      layout(sliderPanel) = BorderPanel.Position.Center
    }
    layout(northedPanel) = BorderPanel.Position.North
  }

  listenToOwnEvents()

  def listenToOwnEvents() = {
    listenTo(slider)
  }

  def deafToOwnEvents() = {
    deafTo(slider)
  }

  def updateUi() = {
    if (target.isDefined) {
      deafToOwnEvents()
      target.foreach(t => slider.value = t.lineThickness.value)
      listenToOwnEvents()
    }
  }

  override def setObject(obj: Option[AnyRef]): Boolean = {
    cleanup()
    obj match {
      case Some(s: HasLineThickness) =>
        target = Some(s)
        listenTo(s.lineThickness)
        updateUi()
        true
      case _ => false
    }
  }

  def cleanup(): Unit = {
    target.foreach(t => deafTo(t.lineThickness))
    target = None
  }

  reactions += {
    case VisualizationProperty.ValueChanged(_) => updateUi()
    case ValueChanged(c) => target.foreach(_.lineThickness.update(slider.value))
  }
}
