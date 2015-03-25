package scalismo.ui.vtk

import scalismo.geometry.{ Point, _3D }
import scalismo.ui.Mesh
import scalismo.ui.Mesh.MeshRenderable3D
import scalismo.utils.MeshConversion
import vtk.vtkPolyData

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
      setGeometry(m, useTemplate = true)
      listenTo(m)
  }

  reactions += {
    case Mesh.GeometryChanged(m) => setGeometry(m, useTemplate = true)
    case Mesh.Reloaded(m) => setGeometry(m, useTemplate = false)
  }

  def setGeometry(mesh: Mesh, useTemplate: Boolean) = this.synchronized {
    val template = if (useTemplate) polyMesh else None
    polyMesh = Some(Caches.MeshCache.getOrCreate(mesh.peer, MeshConversion.meshToVtkPolyData(mesh.peer, template)))

    normals.RemoveAllInputs()
    normals.SetInputData(polyMesh.get)
    normals.Update()

    mapper.RemoveAllInputs()
    mapper.SetInputData(normals.GetOutput())

    mapper.Modified()

    publishEdt(VtkContext.RenderRequest(this))
  }

  def clicked(point: Point[_3D]) = {
    source.meshOrNone.map(m => m.addLandmarkAt(point))
  }

  override def onDestroy() = this.synchronized {
    source.meshOrNone.map(m => deafTo(m))
    super.onDestroy()
    normals.Delete()
    // do NOT delete polymesh, it may be shared.
  }
}