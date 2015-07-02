package scalismo.ui.vtk

import scalismo.geometry.{ _3D, Point }
import scalismo.ui.MeshView.MeshRenderable
import scalismo.ui.visualization.props.{ ColorProperty, LineWidthProperty, OpacityProperty }
import scalismo.ui.{ BoundingBox, MeshView, TwoDViewport }
import scalismo.utils.MeshConversion
import vtk._

object MeshActor {
  def apply(vtkViewport: VtkViewport, source: MeshRenderable): RenderableActor = {
    vtkViewport.viewport match {
      case vp2d: TwoDViewport => new MeshActor2D(vp2d, source)
      case _ => new MeshActor3D(source)
    }
  }
}

trait MeshActor extends ActorColor with ActorOpacity {
  def renderable: MeshRenderable

  override lazy val color: ColorProperty = renderable.color
  override lazy val opacity: OpacityProperty = renderable.opacity

  private def underlyingToPolyData(template: Option[vtkPolyData]): vtkPolyData = {
    Caches.MeshCache.getOrCreate(renderable.source.source, MeshConversion.meshToVtkPolyData(renderable.source.source, template))
  }

  protected var polydata: vtkPolyData = underlyingToPolyData(None)

  protected def onGeometryChanged(): Unit

  protected def rerender(geometryChanged: Boolean = false, canUseTemplate: Boolean = true) = {
    if (geometryChanged) {
      val template = if (canUseTemplate) Some(polydata) else None
      polydata = underlyingToPolyData(template)
      onGeometryChanged()
    }

    publishEdt(VtkContext.RenderRequest(this))
  }

  protected def onInstantiated(): Unit = {}

  override def onDestroy() = this.synchronized {
    deafTo(renderable.source)
    super.onDestroy()
  }

  reactions += {
    case MeshView.GeometryChanged(m) => rerender(geometryChanged = true)
    case MeshView.Reloaded(m) => rerender(geometryChanged = true, canUseTemplate = false)
  }

  listenTo(renderable.source)

  onInstantiated()

  rerender(geometryChanged = true)
}

class MeshActor2D(viewport: TwoDViewport, override val renderable: MeshRenderable) extends TwoDSlicingActor(viewport) with MeshActor with ActorLineWidth {
  override def lineWidth: LineWidthProperty = renderable.lineWidth

  override protected def onSlicePositionChanged(): Unit = rerender()

  override protected def onGeometryChanged(): Unit = {
    planeCutter.SetInputData(polydata)
    planeCutter.Modified()
  }

  override protected def sourceBoundingBox: BoundingBox = VtkUtils.bounds2BoundingBox(polydata.GetBounds())
}

class MeshActor3D(override val renderable: MeshRenderable) extends SinglePolyDataActor with ClickableActor with MeshActor {

  // not declaring this as lazy causes all sorts of weird vtk errors, probably because the methods which use
  // it are invoked from the superclass constructor (at which time this class is not necessarily fully initialized)(?)
  lazy val normals = new vtk.vtkPolyDataNormals() {
    ComputePointNormalsOn()
    ComputeCellNormalsOff()
  }

  override protected def onInstantiated(): Unit = {
    mapper.SetInputConnection(normals.GetOutputPort())
  }

  override protected def onGeometryChanged() = {
    normals.RemoveAllInputs()
    normals.SetInputData(polydata)
    normals.Update()
  }

  override def clicked(point: Point[_3D]): Unit = renderable.source.addLandmarkAt(point)
}
