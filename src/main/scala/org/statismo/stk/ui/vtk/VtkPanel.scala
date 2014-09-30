package org.statismo.stk.ui.vtk

import java.awt.BorderLayout
import java.io.File

import scala.swing.Component
import scala.util.Try

import org.statismo.stk.ui.{EdtPublisher, Viewport, Workspace}

import javax.swing.JPanel
import vtk.vtkPNGWriter
import vtk.vtkWindowToImageFilter
import org.statismo.stk.ui.swing.ViewportPanel

class VtkPanel extends Component with EdtPublisher {
  lazy val canvas = new VtkCanvas(this)
  lazy val vtkViewport: VtkViewport = new VtkViewport(this, canvas.getRenderer)
  listenTo(vtkViewport)

  protected[vtk] var viewportOption: Option[Viewport] = None
  protected[vtk] var workspaceOption: Option[Workspace] = None

  override lazy val peer = {
    val panel = new JPanel(new BorderLayout())
    panel.add(canvas.getComponent, BorderLayout.CENTER)
    panel
  }

  reactions += {
    case VtkContext.RenderRequest(s, immediately) =>
      canvas.render(immediately)
    case VtkContext.ResetCameraRequest(s) =>
      resetCamera()
  }

  def attach(source: ViewportPanel) = {
    viewportOption = source.viewportOption
    workspaceOption = source.workspaceOption
    vtkViewport.attach()
    workspaceOption.map(listenTo(_))
  }

  def detach() = {
    workspaceOption.map(deafTo(_))
    vtkViewport.detach()
    canvas.disableDeferredRendering()
    workspaceOption = None
    viewportOption = None
  }

  def resetCamera() = {
    vtkViewport.resetCamera()
  }

  def screenshot(file: File) = Try {
    val filter = new vtkWindowToImageFilter
    filter.SetInput(canvas.getRenderWindow)
    filter.SetInputBufferTypeToRGBA()
    filter.Update()

    val writer = new vtkPNGWriter
    writer.SetFileName(file.getAbsolutePath)
    writer.SetInputConnection(filter.GetOutputPort())
    writer.Write()
    writer.Delete()
    filter.Delete()
  }
}