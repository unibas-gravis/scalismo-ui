package org.statismo.stk.ui.vtk

import scala.collection.Seq
import org.statismo.stk.ui.Mesh
import vtk.vtkActor
import vtk.vtkPolyData
import vtk.vtkPolyDataMapper
import org.statismo.stk.core.utils.MeshConversion
import org.statismo.stk.core.geometry.Point3D

class MeshActor(source: Mesh) extends PolyDataActor with ColorableActor with ClickableActor {
  override lazy val colorable = source
  listenTo(source)

  private var mesh: Option[vtkPolyData] = None

  setGeometry()

  reactions += {
    case Mesh.GeometryChanged(m) => setGeometry
  }

  def setGeometry() {
    mesh = Some(MeshConversion.meshToVTKPolyData(source.triangleMesh, mesh))
    mapper.SetInputData(mesh.get)
    mapper.Modified()
    publish(VtkContext.RenderRequest(this))
  }

  def clicked(point: Point3D) = {
    source.addLandmarkAt(point)
  }

  override def onDestroy() {
    deafTo(source)
    super.onDestroy()
  }
}