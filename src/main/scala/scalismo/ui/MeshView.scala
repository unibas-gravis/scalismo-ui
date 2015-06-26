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

object MeshView {

  case class GeometryChanged(source: MeshView) extends Event

  case class Reloaded(source: MeshView) extends Event

  class MeshRenderable3D(source: MeshView, override val color: ColorProperty, override val opacity: OpacityProperty) extends Renderable with HasColorAndOpacity with Reactor {
    var meshOrNone: Option[MeshView] = Some(source)
    listenTo(source)
    reactions += {
      case SceneTreeObject.Destroyed(m) =>
        deafTo(m)
        meshOrNone = None
    }
  }

  class MeshRenderable2DOutline(source: MeshView, override val color: ColorProperty, override val opacity: OpacityProperty, override val lineWidth: LineWidthProperty) extends Renderable with HasColorAndOpacity with HasLineWidth with Reactor {
    var meshOrNone: Option[MeshView] = Some(source)
    listenTo(source)
    reactions += {
      case SceneTreeObject.Destroyed(m) =>
        deafTo(m)
        meshOrNone = None
    }
  }

  object DefaultVisualizationStrategy extends VisualizationStrategy[MeshView] {
    override def renderablesFor2D(t: MeshView): Seq[Renderable] = {
      Seq(new MeshRenderable2DOutline(t, t.color, t.opacity, t.lineWidth))
    }
    override def renderablesFor3D(t: MeshView): Seq[Renderable] = {
      Seq(new MeshRenderable3D(t, t.color, t.opacity))
    }
  }

  def createFromUnderlying(peer: TriangleMesh, parent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit scene: Scene): StaticMeshView = {
    new StaticMeshView(new ImmutableReloader[TriangleMesh](peer), parent, name)
  }

  def createFromFile(file: File, parent: Option[StaticThreeDObject], name: String)(implicit scene: Scene): Try[StaticMeshView] = StaticMeshView.createFromFile(file, parent, name)
}

trait MeshView extends UIView[TriangleMesh] with ThreeDRepresentation[MeshView] with Landmarkable with Saveable with HasColorAndOpacity with HasLineWidth {

  override val color: ColorProperty = new ColorProperty(None)
  override val opacity: OpacityProperty = new OpacityProperty(None)
  override val lineWidth: LineWidthProperty = new LineWidthProperty(None)

  override def saveToFile(file: File): Try[Unit] = {
    MeshIO.writeMesh(underlying, file)
  }

  protected[ui] override lazy val saveableMetadata = StaticMeshView

  override def visualizationStrategy: VisualizationStrategy[MeshView] = MeshView.DefaultVisualizationStrategy

  def createLandmarkUncertainty(point: Point[_3D]): Uncertainty[_3D] = {
    val normal = underlying.normalAtPoint(point)
    val rotationMatrix = Uncertainty.Util.rotationMatrixFor(Uncertainty.Util.X3, normal)
    Uncertainty(rotationMatrix, Uncertainty.defaultStdDevs3D)
  }
}

object StaticMeshView extends SceneTreeObjectFactory[StaticMeshView] with FileIoMetadata {
  override val description = "Static Mesh"
  override val fileExtensions = immutable.Seq("vtk", "stl")
  protected[ui] override val ioMetadata = this

  protected[ui] override def tryCreate(file: File)(implicit scene: Scene): Try[StaticMeshView] = {
    createFromFile(file, None, file.getName)
  }

  protected[ui] def createFromFile(file: File, parent: Option[StaticThreeDObject], name: String)(implicit scene: Scene): Try[StaticMeshView] = {
    Try {
      new FileReloader[TriangleMesh](file) {
        override def doLoad() = MeshIO.readMesh(file)
      }
    }.map(reloader => new StaticMeshView(reloader, parent, Some(name)))
  }

}

class StaticMeshView private[ui] (peerLoader: Reloader[TriangleMesh], initialParent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit override val scene: Scene) extends MeshView with Reloadable {

  override def underlying = _peer

  private var _peer = peerLoader.load().get

  name_=(name.getOrElse(Nameable.NoName))

  override def reload() = {
    peerLoader.load() match {
      case ok @ Success(newPeer) =>
        if (newPeer != _peer) {
          _peer = newPeer
          publishEdt(MeshView.Reloaded(this))
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
