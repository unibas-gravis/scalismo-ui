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

object Mesh {

  case class GeometryChanged(source: Mesh) extends Event

  case class Reloaded(source: Mesh) extends Event

  class MeshRenderable3D(source: Mesh, override val color: ColorProperty, override val opacity: OpacityProperty) extends Renderable with HasColorAndOpacity with Reactor {
    var meshOrNone: Option[Mesh] = Some(source)
    listenTo(source)
    reactions += {
      case SceneTreeObject.Destroyed(m) =>
        deafTo(m)
        meshOrNone = None
    }
  }

  class MeshRenderable2DOutline(source: Mesh, override val color: ColorProperty, override val opacity: OpacityProperty, override val lineWidth: LineWidthProperty) extends Renderable with HasColorAndOpacity with HasLineWidth with Reactor {
    var meshOrNone: Option[Mesh] = Some(source)
    listenTo(source)
    reactions += {
      case SceneTreeObject.Destroyed(m) =>
        deafTo(m)
        meshOrNone = None
    }
  }

  object DefaultVisualizationStrategy extends VisualizationStrategy[Mesh] {
    override def renderablesFor2D(t: Mesh): Seq[Renderable] = {
      Seq(new MeshRenderable2DOutline(t, t.color, t.opacity, t.lineWidth))
    }
    override def renderablesFor3D(t: Mesh): Seq[Renderable] = {
      Seq(new MeshRenderable3D(t, t.color, t.opacity))
    }
  }

}

trait Mesh extends ThreeDRepresentation[Mesh] with Landmarkable with Saveable with HasColorAndOpacity with HasLineWidth {

  override val color: ColorProperty = new ColorProperty(None)
  override val opacity: OpacityProperty = new OpacityProperty(None)
  override val lineWidth: LineWidthProperty = new LineWidthProperty(None)

  def peer: TriangleMesh

  override def saveToFile(file: File): Try[Unit] = {
    MeshIO.writeMesh(peer, file)
  }

  protected[ui] override lazy val saveableMetadata = StaticMesh

  override def visualizationStrategy: VisualizationStrategy[Mesh] = Mesh.DefaultVisualizationStrategy

  def createLandmarkUncertainty(point: Point[_3D]): Uncertainty[_3D] = {
    val normal = peer.normalAtPoint(point)
    val rotationMatrix = Uncertainty.Util.rotationMatrixFor(Uncertainty.Util.X3, normal)
    Uncertainty(rotationMatrix, Uncertainty.defaultStdDevs3D)
  }
}