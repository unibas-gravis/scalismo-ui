package scalismo.ui.swing.props

import javax.swing.border.TitledBorder

import scalismo.ui.Scene.SlicingPosition.Precision.valueToPrecisionVal
import scalismo.ui._
import scalismo.ui.swing.util.{ EdtSlider, FancySlider }

import scala.swing.GridBagPanel.{ Anchor, Fill }
import scala.swing._
import scala.swing.event.{ ButtonClicked, ValueChanged }

class SlicingPositionPanel extends BorderPanel with PropertyPanel {
  val description = "Slicing Position"

  private var slicingPosition: Option[Scene.SlicingPosition] = None

  private[SlicingPositionPanel] class AxisControl(axis: Axis.Value) {
    val nameLabel = new Label(axis.toString)
    val slider = new FancySlider {

      min = 0
      max = 0
      value = 0

      override def formattedValue(sliderValue: Int): String = slicingPosition.map(s => s.precision.format(s.precision.fromInt(sliderValue))).getOrElse("?")
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

    def update() = {
      val sp = slicingPosition.get
      val (min, max, value) = axis match {
        case Axis.X => (sp.boundingBox.xMin, sp.boundingBox.xMax, sp.x)
        case Axis.Y => (sp.boundingBox.yMin, sp.boundingBox.yMax, sp.y)
        case Axis.Z => (sp.boundingBox.zMin, sp.boundingBox.zMax, sp.z)
      }
      slider.min = sp.precision.toIntValue(min)
      slider.max = sp.precision.toIntValue(max)
      slider.value = sp.precision.toIntValue(value)
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

    val opacityLabel = new Label("3D: slice opacity")
    flow.contents ++= Seq(opacityLabel, opacity)

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
      axisControls.foreach(_.update())
      visibility.slicesVisible.selected = sp.slicesVisible
      visibility.intersectionsVisible.selected = sp.intersectionsVisible
      visibility.opacity.value = (sp.opacity * 100).toInt
      Seq(visibility.intersectionsVisible, visibility.opacityLabel, visibility.opacity) foreach {
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
