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
  lazy val vtkUi = new VtkCanvas2(this)
  lazy val vtkViewport: VtkViewport = new VtkViewport(this, vtkUi.getRenderer(), vtkUi.interactor)
  listenTo(vtkViewport)

  protected[vtk] var viewportOption: Option[Viewport] = None
  protected[vtk] var workspaceOption: Option[Workspace] = None

  override lazy val peer = {
    val panel = new JPanel(new BorderLayout())
    panel.add(vtkUi.getComponent, BorderLayout.CENTER)
    panel
  }

  //  {
  //    if (!workspace.scene.visualizables(d => d.isVisibleIn(viewport) && d.isInstanceOf[VisualizableSceneTreeObject[_]]).isEmpty) {
  //      vtkUi.Render()
  //    }
  //  }

  reactions += {
    case VtkContext.RenderRequest(s, immediately) =>
      vtkUi.render(immediately)
    case VtkContext.ResetCameraRequest(s) =>
      resetCamera()
  }

  //override lazy val target = vtkUi

  def attach(source: ViewportPanel) = {
    //super.attach(source)
    //vtkUi.GetRenderWindow().SetOffScreenRendering(0)
    viewportOption = source.viewportOption
    workspaceOption = source.workspaceOption
    workspaceOption.map(listenTo(_))
    vtkViewport.attach()
  }

  def detach() = {
    workspaceOption.map(deafTo(_))
    //vtkUi.GetRenderWindow().SetOffScreenRendering(1)
    vtkViewport.detach()
    vtkUi.disableDeferredRendering()
    workspaceOption = None
    viewportOption = None
    //super.detach()
  }

  def resetCamera() = {
    vtkViewport.resetCamera()
  }

  def screenshot(file: File) = Try {
    val filter = new vtkWindowToImageFilter
    filter.SetInput(vtkUi.getRenderWindow())
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