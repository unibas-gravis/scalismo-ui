package scalismo.ui

import java.io.File

import scalismo.geometry.{ Point, _3D }
import scalismo.io.MeshIO
import scalismo.mesh.TriangleMesh
import scalismo.ui.visualization._
import scalismo.ui.visualization.props._

import scala.collection.immutable.Seq
import scala.swing.Reactor
import scala.swing.event.Event
import scala.util.Try

object Mesh extends SimpleVisualizationFactory[Mesh] {

  case class GeometryChanged(source: Mesh) extends Event

  case class Reloaded(source: Mesh) extends Event

  visualizations += Tuple2(Viewport.ThreeDViewportClassName, Seq(new Visualization3D(None)))
  visualizations += Tuple2(Viewport.TwoDViewportClassName, Seq(new Visualization2DOutline(None)))

  class Visualization3D(from: Option[Visualization3D]) extends Visualization[Mesh] with HasColorAndOpacity {
    override val color: ColorProperty = if (from.isDefined) from.get.color.derive() else new ColorProperty(None)
    override val opacity: OpacityProperty = if (from.isDefined) from.get.opacity.derive() else new OpacityProperty(None)

    protected def createDerived() = new Visualization3D(Some(this))

    protected def instantiateRenderables(source: Mesh) = {
      Seq(new MeshRenderable3D(source, color, opacity))
    }

    override val description = "Mesh"
  }

  class MeshRenderable3D(source: Mesh, override val color: ColorProperty, override val opacity: OpacityProperty) extends Renderable with HasColorAndOpacity with Reactor {
    var meshOrNone: Option[Mesh] = Some(source)
    listenTo(source)
    reactions += {
      case SceneTreeObject.Destroyed(m) =>
        deafTo(m)
        meshOrNone = None
    }
  }

  class Visualization2DOutline(from: Option[Visualization2DOutline]) extends Visualization[Mesh] with HasColorAndOpacity with HasLineThickness {
    override val color: ColorProperty = if (from.isDefined) from.get.color.derive() else new ColorProperty(None)
    override val opacity: OpacityProperty = if (from.isDefined) from.get.opacity.derive() else new OpacityProperty(None)
    override val lineThickness: LineThicknessProperty = if (from.isDefined) from.get.lineThickness.derive() else new LineThicknessProperty(None)

    protected def createDerived() = new Visualization2DOutline(Some(this))

    protected def instantiateRenderables(source: Mesh) = {
      Seq(new MeshRenderable2DOutline(source, color, opacity, lineThickness))
    }

    override val description = "Outline"

  }

  class MeshRenderable2DOutline(source: Mesh, override val color: ColorProperty, override val opacity: OpacityProperty, override val lineThickness: LineThicknessProperty) extends Renderable with HasColorAndOpacity with HasLineThickness with Reactor {
    var meshOrNone: Option[Mesh] = Some(source)
    listenTo(source)
    reactions += {
      case SceneTreeObject.Destroyed(m) =>
        deafTo(m)
        meshOrNone = None
    }
  }

}

trait Mesh extends ThreeDRepresentation[Mesh] with Landmarkable with Saveable {
  def peer: TriangleMesh

  override def saveToFile(file: File): Try[Unit] = {
    MeshIO.writeMesh(peer, file)
  }

  protected[ui] override lazy val saveableMetadata = StaticMesh

  protected[ui] override def visualizationProvider: VisualizationProvider[Mesh] = Mesh

  def createLandmarkUncertainty(point: Point[_3D]): Uncertainty[_3D] = {
    val normal = peer.normalAtPoint(point)
    val rotationMatrix = Uncertainty.Util.rotationMatrixFor(Uncertainty.Util.X3, normal)
    Uncertainty(rotationMatrix, Uncertainty.defaultStdDevs3D)
  }
}