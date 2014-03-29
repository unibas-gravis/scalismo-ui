package org.statismo.stk.ui.vtk

import org.statismo.stk.core.geometry.Point3D
import org.statismo.stk.core.utils.MeshConversion
import org.statismo.stk.ui._

import _root_.vtk.{vtkImageActor, vtkActor, vtkPolyData}
import org.statismo.stk.ui.Mesh.MeshRenderable2DOutline
import scala.Some

class ImageActor2D(source: Image3D.Renderable2D)(implicit vtkViewport: VtkViewport) extends RenderableActor {

  override def currentBoundingBox = BoundingBox.None

  override def vtkActors = ???

  val viewport = vtkViewport.viewport.asInstanceOf[TwoDViewport]
  val scene = source.source.scene
  val a = new vtkActor
  val ia = new vtkImageActor

//  listenTo(source.mesh, scene)

  /*
  private var polyMesh: Option[vtkPolyData] = Some(MeshConversion.meshToVTKPolyData(source.mesh.peer, None))


  val plane = new vtk.vtkPlane()
  plane.SetOrigin(0, 0, 0)
  viewport.axis match {
    case Axis.X => plane.SetNormal(-1, 0, 0)
    case Axis.Y => plane.SetNormal(0, -1, 0)
    case Axis.Z => plane.SetNormal(0, 0, -1)
  }

  val cutEdges = new vtk.vtkCutter()
  cutEdges.SetInputData(polyMesh.get)
  cutEdges.SetCutFunction(plane)
  cutEdges.SetValue(0, 0)

  mapper.SetInputConnection(cutEdges.GetOutputPort())

  def update(withGeometry: Boolean = false) = {
    if (withGeometry) {
      polyMesh = Some(MeshConversion.meshToVTKPolyData(source.mesh.peer, polyMesh))
      cutEdges.SetInputData(polyMesh.get)
    }
    cutEdges.SetValue(0, viewport.axis match {
      case Axis.X => scene.slicingPosition.x
      case Axis.Y => scene.slicingPosition.y
      case Axis.Z => scene.slicingPosition.z
    })
    cutEdges.Update()
    publish(VtkContext.ResetCameraRequest(this))
    publish(VtkContext.RenderRequest(this))
  }

  this.GetProperty().SetInterpolationToGouraud()
  update()
  */

  /*
  reactions += {
    case Scene.SlicingPosition.PointChanged(s) => update()
    case Mesh.GeometryChanged(m) => update(true)
  }
  */

  override def onDestroy() = this.synchronized {
    //deafTo(source.mesh, scene)
    super.onDestroy()
  }

}