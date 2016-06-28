package scalismo.ui.rendering.actor

import scalismo.ui.control.SlicingPosition.renderable
import scalismo.ui.model.capabilities.Transformable
import scalismo.ui.model.properties._
import scalismo.ui.model.{ TransformationGlyphNode, BoundingBox, VectorFieldNode }
import scalismo.ui.rendering.actor.mixin._
import scalismo.ui.rendering.util.VtkUtil
import scalismo.ui.view.{ ViewportPanel, ViewportPanel2D, ViewportPanel3D }
import vtk._

object VectorFieldActor extends SimpleActorsFactory[VectorFieldNode] {
  override def actorsFor(renderable: VectorFieldNode, viewport: ViewportPanel): Option[Actors] = {
    viewport match {
      case _3d: ViewportPanel3D => Some(new VectorFieldActor3D(renderable))
      case _2d: ViewportPanel2D => Some(new VectorFieldActor2D(renderable, _2d))
    }
  }
}

trait VectorFieldActor extends SinglePolyDataActor with ActorOpacity with ActorScalarRange with ActorSceneNode {
  override def sceneNode: VectorFieldNode

  override def opacity: OpacityProperty = sceneNode.opacity

  override def scalarRange: ScalarRangeProperty = sceneNode.scalarRange

  protected def onInstantiated(): Unit

  lazy val arrow = new vtkArrowSource

  def setupPolyData(): vtkPolyData = {

    val points = new vtkPoints
    val vectors = new vtkFloatArray() {
      SetNumberOfComponents(3)
    }

    val scalars = new vtkFloatArray() {
      SetNumberOfComponents(1)
    }

    for (((point, vector), i) <- sceneNode.source.pointsWithValues.zipWithIndex) {
      points.InsertNextPoint(point(0), point(1), point(2))
      vectors.InsertNextTuple3(vector(0), vector(1), vector(2))
      scalars.InsertNextValue(vector.norm)
    }

    new vtkPolyData {
      SetPoints(points)
      GetPointData().SetVectors(vectors)
      GetPointData().SetScalars(scalars)
    }
  }

  lazy val polydata = setupPolyData()

  lazy val glyph = new vtkGlyph3D {
    SetSourceConnection(arrow.GetOutputPort)
    SetInputData(polydata)
    //    ScalingOn()
    OrientOn()

    SetScaleModeToScaleByVector()
    SetVectorModeToUseVector()
    SetColorModeToColorByScalar()
  }

  mapper.SetInputConnection(glyph.GetOutputPort)
  mapper.ScalarVisibilityOn()

  def rerender(geometryChanged: Boolean) = {
    arrow.Modified()
    glyph.Update()
    glyph.Modified()
    mapper.Modified()
    actorChanged(geometryChanged)
  }

  listenTo(sceneNode)

  reactions += {
    case Transformable.event.GeometryChanged(_) => rerender(true)
    case NodeProperty.event.PropertyChanged(p) => rerender(false)
  }

  onInstantiated()

  rerender(true)

}

class VectorFieldActor3D(override val sceneNode: VectorFieldNode) extends VectorFieldActor {
  override protected def onInstantiated(): Unit = {
    mapper.SetInputConnection(glyph.GetOutputPort)
  }

}

class VectorFieldActor2D(override val sceneNode: VectorFieldNode, viewport: ViewportPanel2D) extends SlicingActor(viewport) with VectorFieldActor with ActorLineWidth {
  override def lineWidth: LineWidthProperty = sceneNode.lineWidth

  override protected def onSlicingPositionChanged(): Unit = rerender(false)

  override protected def onInstantiated(): Unit = {
    planeCutter.SetInputConnection(glyph.GetOutputPort())
  }

  override protected def sourceBoundingBox: BoundingBox = VtkUtil.bounds2BoundingBox(polydata.GetBounds())

}
