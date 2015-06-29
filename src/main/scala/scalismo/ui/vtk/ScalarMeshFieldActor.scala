package scalismo.ui.vtk

import scalismo.ui.ScalarMeshField.ScalarMeshFieldRenderable3D
import scalismo.ui.visualization.props.ColorProperty
import scalismo.utils.MeshConversion

class ScalarMeshFieldActor(renderable: ScalarMeshFieldRenderable3D) extends PolyDataActor with ColorableActor {

  override lazy val color = new ColorProperty(None)
  override lazy val opacity = renderable.opacity

  val meshData = renderable.source.peer.asInstanceOf[scalismo.mesh.ScalarMeshField[Float]]
  val vtkpd = MeshConversion.scalarMeshFieldToVtkPolyData(meshData)

  mapper.SetInputData(vtkpd)
  mapper.ScalarVisibilityOn()
  mapper.SetScalarRange(meshData.values.min, meshData.values.max)

  this.GetProperty().SetInterpolationToGouraud()
  setGeometry()

  def setGeometry() = this.synchronized {
    vtkpd.Modified()
    mapper.Modified()
    publishEdt(VtkContext.RenderRequest(this))
  }

  override def onDestroy() = this.synchronized {
    super.onDestroy()
  }
}