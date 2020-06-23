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

package scalismo.ui.view

import javax.swing.border.TitledBorder
import javax.swing.{BorderFactory, SwingConstants}
import scalismo.ui.control.SlicingPosition
import scalismo.ui.event.ScalismoPublisher
import scalismo.ui.model.{Axis, BoundingBox, Scene}
import scalismo.ui.rendering.{RendererPanel, RendererState}
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.util.FileIoMetadata
import scalismo.ui.view.action.SaveAction
import scalismo.ui.view.util.{AxisColor, ScalableUI, TypedSlider, TypedSliderValueChanged}

import scala.swing._
import scala.swing.event.{Event, ValueChanged}

object ViewportPanel {

  object event {

    case class BoundingBoxChanged(source: ViewportPanel) extends Event

    case class Detached(source: ViewportPanel) extends Event

  }

}

sealed abstract class ViewportPanel(val frame: ScalismoFrame) extends BorderPanel with ScalismoPublisher {
  def name: String

  def scene: Scene = frame.scene

  val rendererPanel = new RendererPanel(this)

  protected val toolBar: ToolBar = new ToolBar {
    floatable = false
    rollover = true
    orientation = Orientation.Horizontal
  }

  def setupToolBar(): Unit = {
    toolBar.add(new Button(new Action(null) {
      override def apply(): Unit = rendererPanel.resetCamera()
    }) {
      tooltip = "Reset Camera"
      icon = BundledIcon.Reset.standardSized()
    })

    toolBar.add(new Button(new Action(null) {
      override def apply(): Unit = {
        new SaveAction(rendererPanel.screenshot, FileIoMetadata.Png, "Save screenshot")(frame).apply()
      }
    }) {
      tooltip = "Screenshot"
      icon = BundledIcon.Screenshot.standardSized()
    })

  }

  def setupLayout(): Unit = {
    layout(toolBar) = BorderPanel.Position.North
    layout(rendererPanel) = BorderPanel.Position.Center
  }

  def setAttached(attached: Boolean): Unit = {
    if (!attached) {
      publishEvent(ViewportPanel.event.Detached(this))
    }
    rendererPanel.setAttached(attached)
  }

  def currentBoundingBox: BoundingBox = rendererPanel.currentBoundingBox

  def rendererState: RendererState = rendererPanel.rendererState

  override def toString: String = name

  // constructor
  border = new TitledBorder(name)

  setupToolBar()
  setupLayout()

}

class ViewportPanel3D(frame: ScalismoFrame, override val name: String = "3D") extends ViewportPanel(frame) {
  override def setupToolBar(): Unit = {
    super.setupToolBar()

    List(Axis.X, Axis.Y, Axis.Z).foreach { axis =>
      val button = new Button(new Action(axis.toString) {
        override def apply(): Unit = {
          rendererPanel.setCameraToAxis(axis)
        }
      }) {
        foreground = AxisColor.forAxis(axis).darker()
      }
      toolBar.add(button)
    }
  }
}

class ViewportPanel2D(frame: ScalismoFrame, val axis: Axis) extends ViewportPanel(frame) {
  override def name: String = axis.toString

  private lazy val positionSlider = new TypedSlider[Int](true, 0.01) {
    slider.peer.setOrientation(SwingConstants.VERTICAL)
  }

  lazy val positionPlusButton = new Button(new Action("+") {
    override def apply(): Unit = positionSlider.up()
  })

  lazy val positionMinusButton = new Button(new Action("-") {
    override def apply(): Unit = positionSlider.down()
  })

  private lazy val sliderPanel = new BorderPanel {
    layout(positionPlusButton) = BorderPanel.Position.North
    layout(positionSlider.slider) = BorderPanel.Position.Center
    layout(positionMinusButton) = BorderPanel.Position.South
  }

  override def setupLayout(): Unit = {
    super.setupLayout()
    layout(sliderPanel) = BorderPanel.Position.East
  }

  // constructor
  border match {
    case titled: TitledBorder => titled.setTitleColor(AxisColor.forAxis(axis).darker())
    case _                    => // unexpected, can't handle
  }

  rendererPanel.border = BorderFactory.createLineBorder(AxisColor.forAxis(axis), ScalableUI.scale(3))

  listenTo(frame.sceneControl.slicingPosition, positionSlider)

  def updateSliderValue(p: scalismo.geometry.Point3D): Unit = {
    val v = axis match {
      case Axis.X => p.x
      case Axis.Y => p.y
      case Axis.Z => p.z
    }
    deafTo(positionSlider)
    positionSlider.value = v
    listenTo(positionSlider)
  }

  def updateSliderMinMax(b: BoundingBox): Unit = {
    val (min, max) = axis match {
      case Axis.X => (b.xMin, b.xMax)
      case Axis.Y => (b.yMin, b.yMax)
      case Axis.Z => (b.zMin, b.zMax)
    }
    deafTo(positionSlider)
    positionSlider.min = min
    positionSlider.max = max
    listenTo(positionSlider)
  }

  def sliderValueChanged(): Unit = {
    val pos = frame.sceneControl.slicingPosition
    axis match {
      case Axis.X => pos.x = positionSlider.value
      case Axis.Y => pos.y = positionSlider.value
      case Axis.Z => pos.z = positionSlider.value
    }
  }

  reactions += {
    case SlicingPosition.event.PointChanged(_, _, current) => updateSliderValue(current)
    case SlicingPosition.event.BoundingBoxChanged(s)       => updateSliderMinMax(s.boundingBox)
    case SlicingPosition.event.PerspectiveChanged(s) =>
      updateSliderMinMax(s.boundingBox)
      updateSliderValue(s.point)
    case TypedSliderValueChanged(s) if s eq positionSlider => sliderValueChanged()
  }
}
