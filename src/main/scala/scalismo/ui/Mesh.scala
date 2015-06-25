package scalismo.ui

import java.io.File

import scalismo.geometry.{ Point, _3D }
import scalismo.io.MeshIO
import scalismo.mesh.TriangleMesh
import scalismo.ui.Reloadable.{ Reloader, ImmutableReloader, FileReloader }
import scalismo.ui.visualization._
import scalismo.ui.visualization.props._

import scala.collection.immutable
import scala.collection.immutable.Seq
import scala.swing.Reactor
import scala.swing.event.Event
import scala.util.{ Failure, Success, Try }

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

object StaticMesh extends SceneTreeObjectFactory[StaticMesh] with FileIoMetadata {
  override val description = "Static Mesh"
  override val fileExtensions = immutable.Seq("vtk", "stl")
  protected[ui] override val ioMetadata = this

  protected[ui] override def tryCreate(file: File)(implicit scene: Scene): Try[StaticMesh] = {
    createFromFile(file, None, file.getName)
  }

  def createFromFile(file: File, parent: Option[StaticThreeDObject], name: String)(implicit scene: Scene): Try[StaticMesh] = {
    Try {
      new FileReloader[TriangleMesh](file) {
        override def doLoad() = MeshIO.readMesh(file)
      }
    }.map(reloader => new StaticMesh(reloader, parent, Some(name)))
  }

  def createFromPeer(peer: TriangleMesh, parent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit scene: Scene): StaticMesh = {
    new StaticMesh(new ImmutableReloader[TriangleMesh](peer), parent, name)
  }
}

class StaticMesh private[StaticMesh] (peerLoader: Reloader[TriangleMesh], initialParent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit override val scene: Scene) extends Mesh with Reloadable {

  override def peer = _peer

  private var _peer = peerLoader.load().get

  name_=(name.getOrElse(Nameable.NoName))

  override def reload() = {
    peerLoader.load() match {
      case ok @ Success(newPeer) =>
        if (newPeer != peer) {
          _peer = newPeer
          publishEdt(Mesh.Reloaded(this))
        }
        Success(())
      case Failure(ex) => Failure(ex)
    }
  }

  override def isCurrentlyReloadable = peerLoader.isReloadable

  override lazy val parent: StaticThreeDObject = initialParent.getOrElse(new StaticThreeDObject(Some(scene.staticObjects), name))

  override def addLandmarkAt(point: Point[_3D], nameOpt: Option[String]) = {
    val landmarks = parent.landmarks
    landmarks.addAt(point, nameOpt, createLandmarkUncertainty(point))
  }

  parent.representations.add(this)
}
