package org.statismo.stk.ui.vtk

import java.awt.BorderLayout
import java.io.File

import scala.swing.Component
import scala.swing.Reactor
import scala.util.Try

import org.statismo.stk.ui.Viewport
import org.statismo.stk.ui.Workspace

import javax.swing.JPanel
import vtk.vtkPNGWriter
import vtk.vtkWindowToImageFilter
import org.statismo.stk.ui.visualization.VisualizableSceneTreeObject
import org.statismo.stk.ui.swing.ViewportRenderingPanel

class VtkPanel(workspace: Workspace, viewport: Viewport) extends ViewportRenderingPanel with Reactor {
  lazy val vtkUi = new VtkCanvas(workspace, viewport)

  override lazy val target = vtkUi

  lazy val vtkViewport = new VtkViewport(viewport, vtkUi.GetRenderer(), vtkUi.interactor)
  listenTo(viewport, vtkViewport)

//  {
//    if (!workspace.scene.visualizables(d => d.isVisibleIn(viewport) && d.isInstanceOf[VisualizableSceneTreeObject[_]]).isEmpty) {
//      vtkUi.Render()
//    }
//  }

  reactions += {
    case Viewport.Destroyed(v) =>
      deafTo(viewport, vtkViewport)
    case VtkContext.RenderRequest(s) =>
      vtkUi.empty = false
      vtkUi.Render()
    case VtkContext.ResetCameraRequest(s) =>
      resetCamera()
    case VtkContext.ViewportEmptyStatus(v, empty) =>
      vtkUi.empty = empty
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