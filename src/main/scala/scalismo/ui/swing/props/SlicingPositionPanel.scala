package scalismo.ui.swing.props

import javax.swing.border.TitledBorder

import scalismo.ui.Scene.SlicingPosition.Precision.valueToPrecisionVal
import scalismo.ui._
import scalismo.ui.swing.util.EdtSlider

import scala.swing.GridBagPanel.Anchor
import scala.swing._
import scala.swing.event.{ ButtonClicked, ValueChanged }

class SlicingPositionPanel extends BorderPanel with PropertyPanel {
  val description = "Slicing Position"

  private var slicingPosition: Option[Scene.SlicingPosition] = None

  private[SlicingPositionPanel] class Dimension(axis: Axis.Value) {
    val nameLabel = new Label(axis.toString + ":")
    val current = new Label(format(0))
    val minimum = new Label(format(0))
    val maximum = new Label(format(0))
    val slider = new EdtSlider {
      min = 0
      max = 0
      value = 0
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
      if (slicingPosition.isDefined) {
        slicingPosition.get.precision.fromInt(slider.value)
      } else 0
    }

    def format(value: Float): String = {
      if (slicingPosition.isDefined) {
        slicingPosition.get.precision.format(value)
      } else {
        "???"
      }
    }

    def update() = {
      val sp = slicingPosition.get
      val (min, max, value) = axis match {
        case Axis.X => (sp.boundingBox.xMin, sp.boundingBox.xMax, sp.x)
        case Axis.Y => (sp.boundingBox.yMin, sp.boundingBox.yMax, sp.y)
        case Axis.Z => (sp.boundingBox.zMin, sp.boundingBox.zMax, sp.z)
      }
      minimum.text = format(min)
      maximum.text = format(max)
      slider.min = sp.precision.toIntValue(min)
      slider.max = sp.precision.toIntValue(max)
      slider.value = sp.precision.toIntValue(value)
      current.text = format(value)
    }

  }

  val x = new Dimension(Axis.X)
  val y = new Dimension(Axis.Y)
  val z = new Dimension(Axis.Z)

  private def dimensions = Seq(x, y, z)

  val position = new GridBagPanel {
    border = new TitledBorder(null, "Position", TitledBorder.LEADING, 0, null, null)

    def add(comp: Component, position: (Int, Int)): Unit = {
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
      super.add(new BorderPanel {
        layout(comp) = BorderPanel.Position.Center
      }, const)
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
      add(dim.control, (3, row))
      add(dim.maximum, (4, row))
    }
  }

  position.add(x, 1)
  position.add(y, 2)
  position.add(z, 3)

  val precision = new GridPanel(1, Scene.SlicingPosition.Precision.values.size) {
    border = new TitledBorder(null, "Precision", TitledBorder.LEADING, 0, null, null)

    private class ValueButton(val value: Scene.SlicingPosition.Precision.Value) extends RadioButton(value.name)

    val g = new ButtonGroup()
    Scene.SlicingPosition.Precision.values.foreach {
      p =>
        g.buttons += new ValueButton(p) {
          reactions += {
            case ButtonClicked(_) => slicingPosition.foreach(sp => sp.precision = p)
          }
        }
    }
    contents ++= g.buttons

    def set(np: Scene.SlicingPosition.Precision.Value): Unit = {
      g.buttons.foreach {
        b =>
          if (b.asInstanceOf[ValueButton].value == np) {
            b.selected = true
          }
      }
    }
  }

  val visibility = new BorderPanel {
    border = new TitledBorder(null, "Visibility", TitledBorder.LEADING, 0, null, null)
    val flow = new FlowPanel

    val slicesVisible = new CheckBox("Show bounding box/slices")
    val intersectionsVisible = new CheckBox("2D: show intersecting slice positions")

    val opacity = new EdtSlider {
      val s = preferredSize
      s.width /= 2
      preferredSize = s
    }

    flow.contents ++= Seq(new Label("3D: slice opacity"), opacity)

    val north = new BorderPanel
    north.layout(slicesVisible) = BorderPanel.Position.West
    north.layout(flow) = BorderPanel.Position.East
    layout(north) = BorderPanel.Position.North
    layout(intersectionsVisible) = BorderPanel.Position.Center
  }

  layout(new BorderPanel {
    layout(new BorderPanel {
      layout(visibility) = BorderPanel.Position.North
      layout(precision) = BorderPanel.Position.Center
    }) = BorderPanel.Position.North
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
    slicingPosition.foreach(sp => deafTo(sp.scene))
    slicingPosition = None
  }

  def updateUi(): Unit = {
    slicingPosition.foreach { sp =>
      deafToOwnEvents()
      precision.set(sp.precision)
      dimensions.foreach {
        d =>
          d.update()
      }
      visibility.slicesVisible.selected = sp.slicesVisible
      visibility.intersectionsVisible.selected = sp.intersectionsVisible
      visibility.opacity.value = (sp.opacity * 100).toInt
      Seq(visibility.intersectionsVisible, visibility.opacity) foreach {
        _.enabled = sp.slicesVisible
      }
      revalidate()
      listenToOwnEvents()
    }
  }

  def deafToOwnEvents() = {
    deafTo(x.slider, y.slider, z.slider, visibility.slicesVisible, visibility.opacity, visibility.intersectionsVisible)
  }

  def listenToOwnEvents() = {
    listenTo(x.slider, y.slider, z.slider, visibility.slicesVisible, visibility.opacity, visibility.intersectionsVisible)
  }

  reactions += {
    case Scene.SlicingPosition.SlicesVisibleChanged(s) => updateUi()
    case Scene.SlicingPosition.BoundingBoxChanged(sp) => updateUi()
    case Scene.SlicingPosition.PrecisionChanged(sp) => updateUi()
    case Scene.SlicingPosition.PointChanged(sp, _, _) => updateUi()
    case ValueChanged(slider: EdtSlider) =>
      slider match {
        case x.slider => slicingPosition.foreach(_.x = x.value)
        case y.slider => slicingPosition.foreach(_.y = y.value)
        case z.slider => slicingPosition.foreach(_.z = z.value)
        case visibility.opacity => slicingPosition.foreach(_.opacity = visibility.opacity.value / 100.0)
      }
    case ButtonClicked(checkbox: CheckBox) =>
      checkbox match {
        case visibility.slicesVisible => slicingPosition.foreach(_.slicesVisible = checkbox.selected)
        case visibility.intersectionsVisible => slicingPosition.foreach(_.intersectionsVisible = checkbox.selected)
      }
  }

  listenToOwnEvents()
}
