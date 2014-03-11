package org.statismo.stk.ui.vtk

import org.statismo.stk.core.geometry.Point3D
import org.statismo.stk.core.utils.MeshConversion
import org.statismo.stk.ui.Mesh

import vtk.vtkPolyData

class MeshActor(source: Mesh) extends PolyDataActor with ColorableActor with ClickableActor {
  override lazy val colorable = source
  listenTo(source)

  private var polyMesh: Option[vtkPolyData] = None

  this.GetProperty().SetInterpolationToGouraud()
  setGeometry()

  reactions += {
    case Mesh.GeometryChanged(m) => setGeometry()
  }

  def setGeometry() = this.synchronized {
    polyMesh = Some(MeshConversion.meshToVTKPolyData(source.peer, polyMesh))

    val normals = new vtk.vtkPolyDataNormals()
    normals.SetInputData(polyMesh.get)
    normals.ComputePointNormalsOn()
    normals.ComputeCellNormalsOff()
    normals.Update()
    mapper.SetInputData(normals.GetOutput())
    mapper.Modified()
    publish(VtkContext.RenderRequest(this))
  }

  def clicked(point: Point3D) = {
    source.addLandmarkAt(point)
  }

  override def onDestroy() = this.synchronized {
    deafTo(source)
    super.onDestroy()
  }
}