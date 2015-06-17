package scalismo.ui

import java.io.File

import scalismo.common.Scalar
import scalismo.geometry.{ Point, _3D }
import scalismo.image.DiscreteScalarImage
import scalismo.io.ImageIO
import scalismo.ui.Reloadable.Reloader
import scalismo.ui.visualization._

import scala.language.existentials
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag
import scala.swing.Reactor
import scala.swing.event.Event
import scala.util.{ Failure, Success, Try }

object Image3D {

  case class Reloaded(source: Image3D[_]) extends Event

  class BaseRenderable[A](source: Image3D[A]) extends Renderable with Reactor {
    private var _imageOrNone: Option[Image3D[_]] = Some(source)

    def imageOrNone = _imageOrNone

    listenTo(source)
    reactions += {
      case SceneTreeObject.Destroyed(s) => _imageOrNone = None
    }
  }

  class Renderable3D[A](source: Image3D[A]) extends BaseRenderable(source)

  class Renderable2D[A](source: Image3D[A]) extends BaseRenderable(source)

  def DefaultVisualizationStrategy[S] = new VisualizationStrategy[Image3D[S]] {
    override def renderablesFor2D(targetObject: Image3D[S]): Seq[Renderable] = Seq(new Renderable2D(targetObject))

    override def renderablesFor3D(targetObject: Image3D[S]): Seq[Renderable] = Seq(new Renderable3D(targetObject))
  }
}

class Image3D[S: Scalar: ClassTag: TypeTag](reloader: Reloader[DiscreteScalarImage[_3D, S]]) extends ThreeDRepresentation[Image3D[S]] with Landmarkable with Saveable with Reloadable {

  override lazy val visualizationStrategy: VisualizationStrategy[Image3D[S]] = Image3D.DefaultVisualizationStrategy

  private var _peer = reloader.load().get

  def peer: DiscreteScalarImage[_3D, S] = _peer

  protected[ui] override lazy val saveableMetadata = StaticImage3D

  protected[ui] def asFloatImage: DiscreteScalarImage[_3D, Float] = peer.map[Float](p => implicitly[Scalar[S]].toFloat(p))

  override def saveToFile(f: File): Try[Unit] = {
    val extension = {
      val dot = f.getName.lastIndexOf('.')
      // dot == 0 example: ".file"
      if (dot > 0) Success(f.getName.substring(dot + 1)) else Failure(new IllegalArgumentException("No file extension given"))
    }
    extension flatMap {
      case "vtk" => ImageIO.writeVTK[_3D, S](peer, f)
      case "nii" | "nia" => ImageIO.writeNifti(peer, f)
      case _ => Failure(new IllegalArgumentException("Unsupported file extension: " + extension.get))
    }
  }

  override def addLandmarkAt(point: Point[_3D], nameOpt: Option[String]) = {
    parent.asInstanceOf[ThreeDObject].landmarks.addAt(point, nameOpt, Uncertainty.defaultUncertainty3D())
  }

  override def reload() = {
    reloader.load() match {
      case (Success(newPeer)) =>
        if (newPeer != _peer) {
          _peer = newPeer
          publishEdt(Image3D.Reloaded(this))
        }
        Success(())
      case Failure(ex) => Failure(ex)
    }
  }

  override def isCurrentlyReloadable = reloader.isReloadable
}
