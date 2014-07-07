package org.statismo.stk.ui.vtk

import org.statismo.stk.core.geometry.Point3D
import org.statismo.stk.core.utils.MeshConversion
import org.statismo.stk.ui.Mesh

import vtk.vtkPolyData
import org.statismo.stk.ui.Mesh.MeshRenderable3D

class MeshActor3D(source: MeshRenderable3D) extends PolyDataActor with ColorableActor with ClickableActor {
  override lazy val color = source.color
  override lazy val opacity = source.opacity

  private var polyMesh: Option[vtkPolyData] = None
  val normals = new vtk.vtkPolyDataNormals() {
    ComputePointNormalsOn()
    ComputeCellNormalsOff()
  }

  this.GetProperty().SetInterpolationToGouraud()
  source.meshOrNone.map {
    m =>
      setGeometry(m)
      listenTo(m)
  }

  reactions += {
    case Mesh.GeometryChanged(m) => setGeometry(m)
  }

  def setGeometry(mesh: Mesh) = this.synchronized {
    val obsolete = polyMesh
    polyMesh = Some(MeshConversion.meshToVTKPolyData(mesh.peer, polyMesh))
    obsolete.map {
      v =>
      //v.Delete()
    }

    normals.RemoveAllInputs()
    normals.SetInputData(polyMesh.get)
    normals.Update()

    mapper.RemoveAllInputs()
    mapper.SetInputData(normals.GetOutput())

    mapper.Modified()

    publishEdt(VtkContext.RenderRequest(this))
  }

  def clicked(point: Point3D) = {
    source.meshOrNone.map(m => m.addLandmarkAt(point))
  }

  override def onDestroy() = this.synchronized {
    source.meshOrNone.map(m => deafTo(m))
    super.onDestroy()
    normals.Delete()
    polyMesh.map(m => m.Delete())
  }

}