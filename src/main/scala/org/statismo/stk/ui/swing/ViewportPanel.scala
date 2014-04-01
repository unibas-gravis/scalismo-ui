package org.statismo.stk.ui.swing

import java.io.File
import scala.swing.{Slider, Action, BorderPanel, Orientation}
import scala.swing.BorderPanel.Position.Center
import scala.util.Try
import org.statismo.stk.ui._
import org.statismo.stk.ui.swing.actions.SaveAction
import org.statismo.stk.ui.vtk.VtkPanel
import javax.swing.border.TitledBorder
import javax.swing.SwingConstants
import scala.swing.event.ValueChanged

object ViewportPanel {
  def apply(workspace: Workspace, viewport: Viewport): ViewportPanel = {
    viewport match {
      case v: ThreeDViewport => new ThreeDViewportPanel(workspace, v)
      case v: TwoDViewport => new TwoDViewportPanel(workspace, v)
      case v => new ViewportPanel(workspace, v)
    }
  }
}

class ViewportPanel(val workspace: Workspace, val viewport: Viewport) extends BorderPanel {

  val title = new TitledBorder(null, viewport.name, TitledBorder.LEADING, 0, null, null)
  border = title
  listenTo(viewport)
  val vtk = new VtkPanel(workspace, viewport)
  layout(vtk) = Center

  reactions += {
    case Nameable.NameChanged(v) =>
      if (v eq viewport) {
        title.setTitle(viewport.name)
        revalidate()
      }
  }

  val toolbar = new Toolbar {
    floatable = false
    rollover = true
    orientation = Orientation.Horizontal
  }

  toolbar.add(new Action("SS") {
    def doSave(file: File): Try[Unit] = vtk.screenshot(file)

    override def apply() = {
      new SaveAction(doSave, PngFileIoMetadata).apply()
    }
  })

}

class ThreeDViewportPanel(workspace: Workspace, viewport: ThreeDViewport) extends ViewportPanel(workspace, viewport) {
  toolbar.add(new Action("RC") {
    override def apply() = {
      vtk.resetCamera()
    }
  })
  layout(toolbar) = BorderPanel.Position.North
}

class TwoDViewportPanel(workspace: Workspace, viewport: TwoDViewport) extends ViewportPanel(workspace, viewport) {
  toolbar.add(new Action("RC") {
    override def apply() = {
      vtk.resetCamera()
    }
  })
  private [TwoDViewportPanel] class VpSlider extends Slider {
    import org.statismo.stk.ui.Scene.SlicingPosition.Precision.valueToPrecisionVal
    peer.setOrientation(SwingConstants.VERTICAL)
    update(viewport.scene.slicingPosition)
    listenTo(viewport.scene)

    reactions += {
      case Scene.SlicingPosition.PointChanged(sp) => update(sp)
      case Scene.SlicingPosition.PrecisionChanged(sp) => update(sp)
      case Scene.SlicingPosition.BoundingBoxChanged(sp) => update(sp)
      case ValueChanged(c) if c eq this =>
        val sp = viewport.scene.slicingPosition
        val value = sp.precision.fromInt(this.value)
        viewport.axis match {
          case Axis.X => sp.x = value
          case Axis.Y => sp.y = value
          case Axis.Z => sp.z = value
        }
    }

    def update(sp: Scene.SlicingPosition) = {
      deafTo(this)
      val (min, max, value) = viewport.axis match {
        case Axis.X => (sp.boundingBox.xMin, sp.boundingBox.xMax, sp.x)
        case Axis.Y => (sp.boundingBox.yMin, sp.boundingBox.yMax, sp.y)
        case Axis.Z => (sp.boundingBox.zMin, sp.boundingBox.zMax, sp.z)
      }
      this.min = sp.precision.toIntValue(min)
      this.max = sp.precision.toIntValue(max)
      this.value = sp.precision.toIntValue(value)
      listenTo(this)
    }
  }
  layout(new VpSlider) = BorderPanel.Position.East
  layout(toolbar) = BorderPanel.Position.North
}
