package scalismo.ui.vtk

import scalismo.geometry.{ Point, _3D }
import scalismo.mesh.{ ScalarMeshField, TriangleCell, TriangleMesh }
import scalismo.ui.ScalarFieldView.ScalarFieldRenderable3D
import scalismo.ui.visualization.VisualizationProperty
import scalismo.utils.MeshConversion
import vtk.{ vtkGlyph3D, vtkSphereSource }

class ScalarFieldActor3D(renderable: ScalarFieldRenderable3D) extends PolyDataActor with ActorOpacity with ClickableActor {
  private lazy val sphere = new vtkSphereSource
  override lazy val opacity = renderable.opacity
  lazy val radius = renderable.radiuses
  listenTo(radius)

  // We do a trick here. We can create a triangle mesh without cells, and use that to define a scalarMeshField.
  // This can then be converted to a vtk polydata, which we use as the input for the glyph
  val scalarField = renderable.source.source
  val pointSet = TriangleMesh(scalarField.domain.points.toIndexedSeq, IndexedSeq[TriangleCell]())
  val scalarMeshData = ScalarMeshField(pointSet, scalarField.data)
  val vtkpd = MeshConversion.scalarMeshFieldToVtkPolyData(scalarMeshData)

  val glyph = new vtkGlyph3D {
    SetSourceConnection(sphere.GetOutputPort)
    SetInputData(vtkpd)
    SetScaleModeToDataScalingOff()
  }

  mapper.SetInputConnection(glyph.GetOutputPort)
  mapper.SetColorModeToMapScalars()
  mapper.ScalarVisibilityOn()
  mapper.SetScalarRange(scalarMeshData.values.min, scalarMeshData.values.max)

  setGeometry()

  reactions += {
    case VisualizationProperty.ValueChanged(s) => if (s eq radius) setGeometry()
  }

  def setGeometry() = this.synchronized {
    sphere.SetRadius(renderable.radiuses.value(0))
    sphere.Modified()
    glyph.Update()
    glyph.Modified()
    mapper.Modified()
    publishEdt(VtkContext.RenderRequest(this))
  }

  override def onDestroy() = this.synchronized {
    deafTo(radius)
    super.onDestroy()
    glyph.Delete()
    sphere.Delete()
  }

  override def clicked(point: Point[_3D]): Unit = {
    // the click is on the surface of a sphere, but actually "means" the center of the sphere.
    val cloudPoints = renderable.source.source.domain.points.toIndexedSeq
    if (cloudPoints.nonEmpty) {
      val vector = point.toVector
      val vectorsWithIndex = cloudPoints.map(_.toVector - vector).zipWithIndex
      val minIndex = vectorsWithIndex.minBy(_._1.norm2)._2
      renderable.source.addLandmarkAt(cloudPoints(minIndex))
    }
  }
}
