package org.statismo.stk.ui.swing

import java.io.File
import javax.swing._
import javax.swing.border.TitledBorder

import org.statismo.stk.ui._
import org.statismo.stk.ui.swing.actions.SaveAction
import org.statismo.stk.ui.swing.util.EdtSlider
import org.statismo.stk.ui.vtk.VtkPanel

import scala.swing.BorderPanel.Position.Center
import scala.swing.{Action, _}
import scala.swing.event.ValueChanged
import scala.util.Try

class ViewportPanel extends BorderPanel {

  protected var viewport: Option[Viewport] = None
  protected var workspace: Option[Workspace] = None
  protected var renderer: VtkPanel = new VtkPanel

  def viewportOption: Option[Viewport] = viewport

  def workspaceOption: Option[Workspace] = workspace

  def show(workspace: Workspace, viewport: Viewport): Unit = this.synchronized {
    hide() // just in case someone forgot to call it
    this.viewport = Some(viewport)
    this.workspace = Some(workspace)
    listenTo(viewport)
    title.setTitle(viewport.name)
    renderer.attach(this)
  }

  def hide() = this.synchronized {
    viewport.map {
      deafTo(_)
    }
    renderer.detach()
  }

  val title = new TitledBorder(null, "", TitledBorder.LEADING, 0, null, null)
  border = title

  layout(renderer) = Center

  reactions += {
    case Nameable.NameChanged(v) =>
      viewport match {
        case None =>
        case Some(vp) =>
          if (v eq vp) {
            title.setTitle(vp.name)
            revalidate()
          }
      }
  }

  val toolbar = new Toolbar {
    floatable = false
    rollover = true
    orientation = Orientation.Horizontal
  }


  toolbar.add(new Action("SS") {
    def doSave(file: File): Try[Unit] = renderer.screenshot(file)

    override def apply() = {
      new SaveAction(doSave, PngFileIoMetadata).apply()
    }
  }).tooltip = "Screenshot"

  toolbar.add(new Action("RC") {
    override def apply() = {
      renderer.resetCamera()
    }
  }).tooltip = "Reset Camera"
}

class ThreeDViewportPanel extends ViewportPanel {
  layout(toolbar) = BorderPanel.Position.North
}

class TwoDViewportPanel extends ViewportPanel {
  override def show(workspace: Workspace, viewport: Viewport): Unit = {
    super.show(workspace, viewport)
    slider.update(viewport.scene.slicingPosition)
    slider.listenTo(viewport.scene)
  }

  override def hide() = {
    viewport map {
      vp => slider.deafTo(vp.scene)
    }
    super.hide()
  }

  private[TwoDViewportPanel] class VpSlider extends EdtSlider {

    import org.statismo.stk.ui.Scene.SlicingPosition.Precision.valueToPrecisionVal

    peer.setOrientation(SwingConstants.VERTICAL)

    reactions += {
      case Scene.SlicingPosition.PointChanged(sp,_,_) => update(sp)
      case Scene.SlicingPosition.PrecisionChanged(sp) => update(sp)
      case Scene.SlicingPosition.BoundingBoxChanged(sp) => update(sp)
      case ValueChanged(c) if c eq this =>
        viewport match {
          case Some(vp: TwoDViewport) =>
            val sp = vp.scene.slicingPosition
            val value = sp.precision.fromInt(this.value)
            vp.axis match {
              case Axis.X => sp.x = value
              case Axis.Y => sp.y = value
              case Axis.Z => sp.z = value
            }
          case _ =>
        }
    }

    def update(sp: Scene.SlicingPosition) = {
      deafTo(this)
      viewport match {
        case Some(vp: TwoDViewport) =>
          val (min, max, value) = vp.axis match {
            case Axis.X => (sp.boundingBox.xMin, sp.boundingBox.xMax, sp.x)
            case Axis.Y => (sp.boundingBox.yMin, sp.boundingBox.yMax, sp.y)
            case Axis.Z => (sp.boundingBox.zMin, sp.boundingBox.zMax, sp.z)
          }
          this.min = sp.precision.toIntValue(min)
          this.max = sp.precision.toIntValue(max)
          this.value = sp.precision.toIntValue(value)
        case _ =>
      }
      listenTo(this)
    }
  }

  private val slider = new VpSlider
  layout(slider) = BorderPanel.Position.East
  layout(toolbar) = BorderPanel.Position.North
}
