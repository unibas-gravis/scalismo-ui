package org.statismo.stk.ui.swing.props

import scala.swing._

import org.statismo.stk.ui._
import scala.swing.GridBagPanel.Anchor
import scala.Some
import org.statismo.stk.core.geometry.Point3D
import javax.swing.border.TitledBorder
import scala.swing.event.{ValueChanged, ButtonClicked}

class SlicingPositionPanel extends BorderPanel with PropertyPanel {
  val description = "Slicing Position"

  private var slicingPosition: Option[Scene.SlicingPosition] = None

  private [SlicingPositionPanel] class Dimension (axis: Axis.Value) {
    val nameLabel = new Label(axis.toString +":")
    val current = new Label(format(0))
    val minimum = new Label(format(0))
    val maximum = new Label(format(0))
    val slider = new Slider {
      min = 0
      max = 0
      value = 0
    }

    def value: Float = {
      if (slicingPosition.isDefined) {
        slicingPosition.get.precision.fromInteger(slider.value)
      } else 0
    }

    def format(value:Float): String = {
      if (slicingPosition.isDefined) {
        slicingPosition.get.precision.format(value)
      } else {
        "???"
      }
    }

    def updateBounds(boundingBox: BoundingBox) = {
      val (min, max) = axis match {
        case Axis.X => (boundingBox.xMin, boundingBox.xMax)
        case Axis.Y => (boundingBox.yMin, boundingBox.yMax)
        case Axis.Z => (boundingBox.zMin, boundingBox.zMax)
      }
      minimum.text = format(min)
      maximum.text = format(max)
      slider.min = slicingPosition.get.precision.toInteger(min)
      slider.max = slicingPosition.get.precision.toInteger(max)
    }

    def updatePoint(point: Point3D) = {
      val actual = axis match {
        case Axis.X => point.x
        case Axis.Y => point.y
        case Axis.Z => point.z
      }
      setValueText(actual)
    }

    private def setValueText(actual: Float) : Unit = {
      current.text = format(actual)
    }
  }

  val x = new Dimension(Axis.X)
  val y = new Dimension(Axis.Y)
  val z = new Dimension(Axis.Z)

  private def dimensions = Seq(x,y,z)

  val position = new GridBagPanel {
    border = new TitledBorder(null, "Position", TitledBorder.LEADING, 0, null, null)

    def add(comp: Component, position: (Int, Int)) : Unit = {
      val const = pair2Constraints(position)
      const.ipadx = 10
      if (position._2 == 0) {
        const.anchor = Anchor.Center
      } else {
        const.anchor = if (position._1 == 0) {
          Anchor.West
        } else if (position._1 == 3) {
          Anchor.Center
        } else Anchor.East
      }
      super.add(new BorderPanel {layout(comp) = BorderPanel.Position.Center}, const)
    }

    add(new Label("Dim."), (0, 0))
    add(new Label("Current"), (1, 0))
    add(new Label("Min."), (2, 0))
    add(new Label("Control"), (3, 0))
    add(new Label("Max."), (4, 0))

    def add(dim: Dimension, row: Int): Unit = {
      add(dim.nameLabel, (0, row))
      add(dim.current, (1, row))
      add(dim.minimum, (2, row))
      add(dim.slider, (3, row))
      add(dim.maximum, (4, row))
    }
  }

  position.add(x, 1)
  position.add(y, 2)
  position.add(z, 3)

  listenTo(x.slider)
  listenTo(y.slider)
  listenTo(z.slider)

  val precision = new GridPanel(1, Scene.SlicingPosition.Precision.values.size) {
    border = new TitledBorder(null, "Precision", TitledBorder.LEADING, 0, null, null)

    private class ValueButton(val value: Scene.SlicingPosition.Precision.Value) extends RadioButton(value.name)

    val g = new ButtonGroup()
    Scene.SlicingPosition.Precision.values.foreach {p =>
      g.buttons += new ValueButton(p) {
        reactions += {
          case ButtonClicked(_) => slicingPosition.map(sp => sp.precision = p)
        }
      }
    }
    contents ++= g.buttons

    def set(np: Scene.SlicingPosition.Precision.Value) {
      g.buttons.foreach { b =>
        if (b.asInstanceOf[ValueButton].value == np) {
          b.selected = true
        }
      }
    }
  }

  layout(new BorderPanel {
    layout(precision) = BorderPanel.Position.North
    layout(position) = BorderPanel.Position.Center
  }) = BorderPanel.Position.North

  def setObject(obj: Option[AnyRef]): Boolean = {
    cleanup()
    obj match {
      case Some(s: Scene) =>
        slicingPosition = Some(s.slicingPosition)
        listenTo(s)
        updateUi()
        true
      case _ => false
    }
  }

  def cleanup() = {
    slicingPosition.map(sp => deafTo(sp.scene))
    slicingPosition = None
  }

  def updateUi() : Unit = {
    slicingPosition.map { sp =>
      precision.set(sp.precision)
      dimensions.foreach { d =>
        d.updatePoint(sp.point)
        d.updateBounds(sp.boundingBox)
      }
    }
  }

  reactions += {
    case Scene.SlicingPosition.PrecisionChanged(sp) =>
      updateUi()
    case Scene.SlicingPosition.PointChanged(sp) =>
      dimensions.foreach { d =>
        d.updatePoint(sp.point)
        d.updateBounds(sp.boundingBox)
      }
    case ValueChanged(slider: Slider) =>
      slider match {
        case x.slider => slicingPosition.map{_.x = x.value}
        case y.slider => slicingPosition.map(_.y = y.value)
        case z.slider => slicingPosition.map(_.z = z.value)
      }
  }
}