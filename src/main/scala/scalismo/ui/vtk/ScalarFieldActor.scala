package scalismo.ui.vtk

import scalismo.geometry.{ Point, _3D }
import scalismo.mesh.{ ScalarMeshField, TriangleCell, TriangleMesh }
import scalismo.ui.ScalarFieldView.ScalarFieldRenderable
import scalismo.ui.visualization.VisualizationProperty
import scalismo.ui.{ BoundingBox, ThreeDViewport, TwoDViewport }
import scalismo.utils.MeshConversion
import vtk.{ vtkGlyph3D, vtkSphereSource }

object ScalarFieldActor {
  def apply(vtkViewport: VtkViewport, source: ScalarFieldRenderable): RenderableActor = {
    vtkViewport.viewport match {
      case vp2d: TwoDViewport => new ScalarFieldActor2D(vp2d, source)
      case vp3d: ThreeDViewport => new ScalarFieldActor3D(vp3d, source)
    }
  }
}

trait ScalarFieldActor extends SinglePolyDataActor with ClickableActor with ActorOpacity with ActorScalarRange {

  def renderable: ScalarFieldRenderable

  override lazy val opacity = renderable.opacity
  override lazy val scalarRange = renderable.scalarRange

  private lazy val scalarField = renderable.source.source
  private lazy val sphere = new vtkSphereSource

  private val radius = renderable.radiuses
  listenTo(radius)

  // We do a trick here. We can create a triangle mesh without cells, and use that to define a scalarMeshField.
  // This can then be converted to a vtk polydata, which we use as the input for the glyph
  private val pointSet = TriangleMesh(scalarField.domain.points.toIndexedSeq, IndexedSeq[TriangleCell]())
  private val scalarMeshData = ScalarMeshField(pointSet, scalarField.data)
  private val vtkpd = MeshConversion.scalarMeshFieldToVtkPolyData(scalarMeshData)
  private val glyph = new vtkGlyph3D {
    SetSourceConnection(sphere.GetOutputPort)
    SetInputData(vtkpd)
    SetScaleModeToDataScalingOff()
  }

  glyph.Update()
  val scalarFieldPolydata = glyph.GetOutput()

  mapper.SetColorModeToMapScalars()
  mapper.ScalarVisibilityOn()

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

  def rerender(): Unit = {
    publishEdt(VtkContext.RenderRequest(this))
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

class ScalarFieldActor3D(viewPort: ThreeDViewport, val renderable: ScalarFieldRenderable) extends SinglePolyDataActor with ScalarFieldActor {
  mapper.SetInputData(scalarFieldPolydata)
}

class ScalarFieldActor2D(viewPort: TwoDViewport, val renderable: ScalarFieldRenderable) extends TwoDSlicingActor(viewPort) with ScalarFieldActor {

  planeCutter.SetInputData(scalarFieldPolydata)

  override protected def onSlicePositionChanged(): Unit = rerender()

  override protected def sourceBoundingBox: BoundingBox = VtkUtils.bounds2BoundingBox(scalarFieldPolydata.GetBounds())
}

