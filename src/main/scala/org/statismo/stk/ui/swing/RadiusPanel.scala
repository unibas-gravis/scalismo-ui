package org.statismo.stk.ui.swing

import org.statismo.stk.ui.SceneTreeObject
import org.statismo.stk.ui.Colorable
import scala.swing.Slider
import scala.swing.BorderPanel
import scala.swing.event.ValueChanged
import scala.swing.Component
import javax.swing.JColorChooser
import scala.swing.Swing
import java.awt.Color
import scala.swing.event.Event
import javax.swing.colorchooser.DefaultSwatchChooserPanel
import org.statismo.stk.ui.swing.util.ColorPickerPanel
import javax.swing.border.TitledBorder
import scala.swing.Label
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JPanel
import java.awt.Graphics
import org.statismo.stk.ui.Radius

class RadiusPanel extends BorderPanel with SceneObjectPropertyPanel {
  val description = "Radius"
  private var target: Option[Radius] = None

  private val slider = new Slider() {
    min = 1
    max = 20
    value = 0
  }

  {
    val northedPanel = new BorderPanel {
      val opacityPanel = new BorderPanel {
        layout(slider) = BorderPanel.Position.Center
        layout(new Label(slider.min.toString)) = BorderPanel.Position.West
        layout(new Label(slider.max.toString)) = BorderPanel.Position.East
        border = new TitledBorder(null, "Radius", TitledBorder.LEADING, 0, null, null)
      }
      layout(opacityPanel) = BorderPanel.Position.Center
    }
    layout(northedPanel) = BorderPanel.Position.North
  }
  listenToOwnEvents()

  reactions += {
    case Colorable.AppearanceChanged(t) => updateUi()
    case ValueChanged(s) => {
      if (target.isDefined) {
        target.get.radius = (s.asInstanceOf[Slider].value.toFloat)
      }
    }
  }
  def listenToOwnEvents() = {
    listenTo(slider)
  }

  def deafToOwnEvents() = {
    deafTo(slider)
  }

  def cleanup() = {
    if (target.isDefined) {
      deafTo(target.get)
      target = None
    }
  }

  def setObject(obj: Option[SceneTreeObject]): Boolean = {
      cleanup()
    if (obj.isDefined && obj.get.isInstanceOf[Radius]) {
      target = Some(obj.get.asInstanceOf[Radius])
      updateUi()
      listenTo(target.get)
      true
    } else {
      false
    }
  }

  def updateUi() = {
    if (target.isDefined) {
      deafToOwnEvents()
      slider.value = (target.get.radius).toInt
      listenToOwnEvents()
    }
  }
}