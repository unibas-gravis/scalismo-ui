package scalismo.ui.swing.props

import javax.swing.border.TitledBorder

import scalismo.geometry.{ Vector, _1D }
import scalismo.ui.swing.util.EdtSlider
import scalismo.ui.visualization.VisualizationProperty
import scalismo.ui.visualization.props.HasRadiuses

import scala.swing.BorderPanel
import scala.swing.event.ValueChanged

class Radius1DPropertyPanel extends BorderPanel with PropertyPanel {
  override def description: String = "Opacity"

  private var target: Option[HasRadiuses[_1D]] = None

  private val radiusSlider = new EdtSlider {
    min = 1
    max = 500
    value = 500
  }

  {
    val northedPanel = new BorderPanel {
      val radiusPanel = new BorderPanel {
        border = new TitledBorder(null, "Radius", TitledBorder.LEADING, 0, null, null)
        layout(radiusSlider) = BorderPanel.Position.Center
      }
      layout(radiusPanel) = BorderPanel.Position.Center
    }
    layout(northedPanel) = BorderPanel.Position.North
  }

  listenToOwnEvents()

  def listenToOwnEvents() = {
    listenTo(radiusSlider)
  }

  def deafToOwnEvents() = {
    deafTo(radiusSlider)
  }

  def updateUi() = {
    if (target.isDefined) {
      deafToOwnEvents()
      target.foreach(t => radiusSlider.value = (t.radiuses.value(0) * 10.0f).toInt)
      listenToOwnEvents()
    }
  }

  override def setObject(obj: Option[AnyRef]): Boolean = {
    cleanup()
    obj match {
      case Some(c: HasRadiuses[_]) if c.radiuses.dimensionality == 1 =>
        target = Some(c.asInstanceOf[HasRadiuses[_1D]])
        listenTo(c.radiuses)
        updateUi()
        true
      case _ => false
    }
  }

  def cleanup(): Unit = {
    target.foreach(t => deafTo(t.radiuses))
    target = None
  }

  reactions += {
    case VisualizationProperty.ValueChanged(_) => updateUi()
    case ValueChanged(c) => target.foreach(_.radiuses.value = Vector(radiusSlider.value.toFloat / 10.0f))
  }

}
