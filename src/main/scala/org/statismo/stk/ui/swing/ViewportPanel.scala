package org.statismo.stk.ui.swing

import java.io.File
import scala.swing.Action
import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position.Center
import scala.swing.Orientation
import scala.util.Try
import org.statismo.stk.ui.Nameable
import org.statismo.stk.ui.PngFileIoMetadata
import org.statismo.stk.ui.ThreeDViewport
import org.statismo.stk.ui.Viewport
import org.statismo.stk.ui.Workspace
import org.statismo.stk.ui.swing.actions.SaveAction
import org.statismo.stk.ui.vtk.VtkPanel
import javax.swing.border.TitledBorder
import org.statismo.stk.ui.SliceViewport

object ViewportPanel {
  def apply(workspace: Workspace, viewport: Viewport): ViewportPanel = {
    viewport match {
      case v: ThreeDViewport => new ThreeDViewportPanel(workspace, v)
      case v: SliceViewport => new SliceViewportPanel(workspace, v)
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
    case Nameable.NameChanged(v) => {
      if (v eq viewport) {
        title.setTitle(viewport.name)
        revalidate
      }
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
      new SaveAction(doSave, PngFileIoMetadata).apply
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

class SliceViewportPanel(workspace: Workspace, viewport: SliceViewport) extends ViewportPanel(workspace, viewport) {
  layout(toolbar) = BorderPanel.Position.North
}
