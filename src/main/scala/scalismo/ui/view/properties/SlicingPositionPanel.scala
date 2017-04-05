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

package scalismo.ui.view.properties

import java.awt.Font
import javax.swing.border.TitledBorder

import scalismo.ui.control.SlicingPosition
import scalismo.ui.model.{ Axis, Scene, SceneNode }
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.util.{ AxisColor, FancySlider }

import scala.swing.GridBagPanel.{ Anchor, Fill }
import scala.swing._
import scala.swing.event.{ ButtonClicked, ValueChanged }

object SlicingPositionPanel extends PropertyPanel.Factory {
  override def create(frame: ScalismoFrame): PropertyPanel = new SlicingPositionPanel(frame)
}

class SlicingPositionPanel(override val frame: ScalismoFrame) extends BorderPanel with PropertyPanel {
  val description = "Slicing Position"

  private var slicingPosition: Option[SlicingPosition] = None

  private[SlicingPositionPanel] class AxisControl(axis: Axis) {
    val nameLabel = new Label(axis.toString) {
      foreground = AxisColor.forAxis(axis).darker()
      font = font.deriveFont(font.getStyle | Font.BOLD)
    }
    val slider = new FancySlider {

      min = 0
      max = 0
      value = 0

      //override def formattedValue(sliderValue: Int): String = slicingPosition.map(s => s.precision.format(s.precision.fromInt(sliderValue))).getOrElse("?")
    }

    val minus = new Button(new Action("-") {
      override def apply(): Unit = {
        if (slider.value > slider.min) {
          slider.value = slider.value - 1
        }
      }
    })

    val plus = new Button(new Action("+") {
      override def apply(): Unit = {
        if (slider.value < slider.max) {
          slider.value = slider.value + 1
        }
      }
    })

    val control = new BorderPanel {
      layout(minus) = BorderPanel.Position.West
      layout(slider) = BorderPanel.Position.Center
      layout(plus) = BorderPanel.Position.East
    }

    def value: Float = {
      slider.value
    }

    def update() = {
      val sp = slicingPosition.get
      val (min, max, value) = axis match {
        case Axis.X => (sp.boundingBox.xMin, sp.boundingBox.xMax, sp.x)
        case Axis.Y => (sp.boundingBox.yMin, sp.boundingBox.yMax, sp.y)
        case Axis.Z => (sp.boundingBox.zMin, sp.boundingBox.zMax, sp.z)
      }
      slider.min = Math.floor(min).toInt
      slider.max = Math.ceil(max).toInt
      slider.value = Math.round(value).toInt
    }

  }

  val x = new AxisControl(Axis.X)
  val y = new AxisControl(Axis.Y)
  val z = new AxisControl(Axis.Z)

  private def axisControls = Seq(x, y, z)

  val position = new GridBagPanel {
    border = new TitledBorder(null, "Position", TitledBorder.LEADING, 0, null, null)

    def add(comp: Component, position: (Int, Int)): Unit = {
      val const = pair2Constraints(position)
      const.ipadx = 10
      const.ipady = 10
      if (position._1 == 1) {
        const.fill = Fill.Both
        const.weightx = 1.0
      } else {
        const.anchor = Anchor.West
      }
      super.add(comp, const)
    }

    def add(axis: AxisControl, row: Int): Unit = {
      add(axis.nameLabel, (0, row))
      add(axis.control, (1, row))
    }
  }
  position.add(x, 1)
  position.add(y, 2)
  position.add(z, 3)

  val slicesVisible = new CheckBox("Show bounding box and slices")

  val visibilityPanel = new BorderPanel {
    border = new TitledBorder(null, "Visibility", TitledBorder.LEADING, 0, null, null)
    layout(slicesVisible) = BorderPanel.Position.Center
  }

  layout(new BorderPanel {
    layout(new BorderPanel {
      layout(visibilityPanel) = BorderPanel.Position.North
    }) = BorderPanel.Position.North
    layout(position) = BorderPanel.Position.Center
  }) = BorderPanel.Position.North

  override def setNodes(nodes: List[SceneNode]): Boolean = {
    cleanup()
    singleMatch[Scene](nodes) match {
      case Some(s) if s == frame.sceneControl.scene =>
        slicingPosition = Some(frame.sceneControl.slicingPosition)
        listenTo(slicingPosition.get)
        updateUi()
        true
      case _ => false
    }
  }

  def cleanup() = {
    slicingPosition.foreach(sp => deafTo(sp))
    slicingPosition = None
  }

  def updateUi(): Unit = {
    slicingPosition.foreach { sp =>
      deafToOwnEvents()
      axisControls.foreach(_.update())
      slicesVisible.selected = sp.visible
      revalidate()
      listenToOwnEvents()
    }
  }

  def deafToOwnEvents() = {
    deafTo(x.slider, y.slider, z.slider, slicesVisible)
  }

  def listenToOwnEvents() = {
    listenTo(x.slider, y.slider, z.slider, slicesVisible)
  }

  reactions += {
    case SlicingPosition.event.VisibilityChanged(_) => updateUi()
    case SlicingPosition.event.BoundingBoxChanged(_) => updateUi()
    case SlicingPosition.event.PointChanged(_, _, _) => updateUi()
    case ValueChanged(slider: Slider) =>
      slider match {
        case x.slider => slicingPosition.foreach(_.x = x.value)
        case y.slider => slicingPosition.foreach(_.y = y.value)
        case z.slider => slicingPosition.foreach(_.z = z.value)
      }
    case ButtonClicked(cb: CheckBox) if cb == slicesVisible => slicingPosition.foreach(_.visible = cb.selected)
  }

  listenToOwnEvents()
}
