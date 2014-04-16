package org.statismo.stk.ui.vtk

import java.awt.BorderLayout
import java.io.File

import scala.swing.Component
import scala.swing.Reactor
import scala.util.Try

import org.statismo.stk.ui.{EdtPublisher, Viewport, Workspace}

import javax.swing.JPanel
import vtk.vtkPNGWriter
import vtk.vtkWindowToImageFilter
import org.statismo.stk.ui.visualization.VisualizableSceneTreeObject
import org.statismo.stk.ui.swing.{ViewportPanel, ViewportRenderingPanel}
import scala.swing.event.Event

class VtkPanel extends ViewportRenderingPanel with EdtPublisher {
  lazy val vtkUi = new VtkCanvas(this)
  lazy val vtkViewport: VtkViewport = new VtkViewport(this, vtkUi.GetRenderer(), vtkUi.interactor)
  listenTo(vtkViewport)

  protected [vtk] var viewportOption: Option[Viewport] = None
  protected [vtk] var workspaceOption: Option[Workspace] = None


  //  {
//    if (!workspace.scene.visualizables(d => d.isVisibleIn(viewport) && d.isInstanceOf[VisualizableSceneTreeObject[_]]).isEmpty) {
//      vtkUi.Render()
//    }
//  }

  reactions += {
    case VtkContext.RenderRequest(s, immediately) =>
      vtkUi.empty = false
      vtkUi.render(immediately)
    case VtkContext.ResetCameraRequest(s) =>
      resetCamera()
    case VtkContext.ViewportEmptyStatus(v, empty) =>
      vtkUi.empty = empty
  }

  override lazy val target = vtkUi

  override def attach(source: ViewportPanel) = {
    viewportOption = source.viewportOption
    workspaceOption = source.workspaceOption
    super.attach(source)
    vtkUi.GetRenderWindow().SetOffScreenRendering(0)
    vtkViewport.attach()
  }

  override def detach() = {
    vtkUi.GetRenderWindow().SetOffScreenRendering(1)
    vtkViewport.detach()
    vtkUi.disableDeferredRendering()
    super.detach()
  }

  override def resetCamera() = {
    vtkViewport.resetCamera()
  }

  override def screenshot(file: File) = Try {
    val filter = new vtkWindowToImageFilter
    filter.SetInput(vtkUi.GetRenderWindow())
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