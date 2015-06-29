package scalismo.ui

import java.io.File

import scalismo.common.Scalar
import scalismo.geometry.{ Point, _3D }
import scalismo.image.DiscreteScalarImage
import scalismo.io.ImageIO
import scalismo.io.ImageIO.ScalarType
import scalismo.ui.Reloadable.{ ImmutableReloader, FileReloader, Reloader }
import scalismo.ui.visualization._
import spire.math.{ ULong, UInt, UShort, UByte }

import scala.collection.immutable
import scala.language.existentials
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag
import scala.swing.Reactor
import scala.swing.event.Event
import scala.util.{ Failure, Success, Try }

object Image3DView {

  case class Reloaded(source: Image3DView[_]) extends Event

  class BaseRenderable[A](source: Image3DView[A]) extends Renderable with Reactor {
    private var _imageOrNone: Option[Image3DView[_]] = Some(source)

    def imageOrNone = _imageOrNone

    listenTo(source)
    reactions += {
      case SceneTreeObject.Destroyed(s) => _imageOrNone = None
    }
  }

  class Renderable3D[A](source: Image3DView[A]) extends BaseRenderable(source)

  class Renderable2D[A](source: Image3DView[A]) extends BaseRenderable(source)

  def DefaultVisualizationStrategy[S] = new VisualizationStrategy[Image3DView[S]] {
    override def renderablesFor2D(targetObject: Image3DView[S]): Seq[Renderable] = Seq(new Renderable2D(targetObject))

    override def renderablesFor3D(targetObject: Image3DView[S]): Seq[Renderable] = Seq(new Renderable3D(targetObject))
  }

  def createFromSource[S: Scalar: ClassTag: TypeTag](source: DiscreteScalarImage[_3D, S], parent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit scene: Scene): StaticImage3DView[S] = {
    new StaticImage3DView(new ImmutableReloader[DiscreteScalarImage[_3D, S]](source), parent, name)
  }

  def createFromFile(file: File, parent: Option[StaticThreeDObject], name: String)(implicit scene: Scene): Try[StaticImage3DView[_]] = {
    StaticImage3DView.createFromFile(file, parent, name)
  }

}

abstract class Image3DView[S: Scalar: ClassTag: TypeTag](reloader: Reloader[DiscreteScalarImage[_3D, S]]) extends UIView[DiscreteScalarImage[_3D, S]] with ThreeDRepresentation[Image3DView[S]] with Landmarkable with Saveable with Reloadable {

  override lazy val visualizationStrategy: VisualizationStrategy[Image3DView[S]] = Image3DView.DefaultVisualizationStrategy

  private var _peer = reloader.load().get

  override def source: DiscreteScalarImage[_3D, S] = _peer

  protected[ui] override lazy val saveableMetadata = StaticImage3DView

  protected[ui] def asFloatImage: DiscreteScalarImage[_3D, Float] = source.map[Float](p => implicitly[Scalar[S]].toFloat(p))

  override def saveToFile(f: File): Try[Unit] = {
    val extension = {
      val dot = f.getName.lastIndexOf('.')
      // dot == 0 example: ".file"
      if (dot > 0) Success(f.getName.substring(dot + 1)) else Failure(new IllegalArgumentException("No file extension given"))
    }
    extension flatMap {
      case "vtk" => ImageIO.writeVTK[_3D, S](source, f)
      case "nii" | "nia" => ImageIO.writeNifti(source, f)
      case _ => Failure(new IllegalArgumentException("Unsupported file extension: " + extension.get))
    }
  }

  override def addLandmarkAt(point: Point[_3D], nameOpt: Option[String]) = {
    parent.landmarks.addAt(point, nameOpt, Uncertainty.defaultUncertainty3D())
  }

  override def reload() = {
    reloader.load() match {
      case (Success(newPeer)) =>
        if (newPeer != _peer) {
          _peer = newPeer
          publishEdt(Image3DView.Reloaded(this))
        }
        Success(())
      case Failure(ex) => Failure(ex)
    }
  }

  override def isCurrentlyReloadable = reloader.isReloadable
}

object StaticImage3DView extends SceneTreeObjectFactory[StaticImage3DView[_]] with FileIoMetadata {
  override val description = "Static 3D Image"
  override val fileExtensions = immutable.Seq("nii", "nia", "vtk")
  protected[ui] override val ioMetadata = this

  protected[ui] override def tryCreate(file: File)(implicit scene: Scene): Try[StaticImage3DView[_]] = {
    createFromFile(file, None, file.getName)
  }

  protected[ui] def createFromFile(file: File, parent: Option[StaticThreeDObject], name: String)(implicit scene: Scene): Try[StaticImage3DView[_]] = {

    def loadAs[T: Scalar: TypeTag: ClassTag]: Try[StaticImage3DView[T]] = {
      val reloaderTry = Try {
        new FileReloader[DiscreteScalarImage[_3D, T]](file) {
          override def doLoad() = ImageIO.read3DScalarImage[T](file)
        }
      }
      reloaderTry.map(r => new StaticImage3DView(r, parent, Some(name)))
    }

    ScalarType.ofFile(file) match {
      case Success(ScalarType.Byte) => loadAs[Byte]
      case Success(ScalarType.Short) => loadAs[Short]
      case Success(ScalarType.Int) => loadAs[Int]
      case Success(ScalarType.Long) => loadAs[Long]
      case Success(ScalarType.Float) => loadAs[Float]
      case Success(ScalarType.Double) => loadAs[Double]
      case Success(ScalarType.UByte) => loadAs[UByte]
      case Success(ScalarType.UShort) => loadAs[UShort]
      case Success(ScalarType.UInt) => loadAs[UInt]
      case Success(ScalarType.ULong) => loadAs[ULong]
      case Failure(e) => Failure(e)
      case _ => Failure(new IllegalStateException("won't happen")) // this is just here to please the compiler
    }

  }

}

class StaticImage3DView[S: Scalar: ClassTag: TypeTag] private[ui] (reloader: Reloader[DiscreteScalarImage[_3D, S]], initialParent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit override val scene: Scene) extends Image3DView[S](reloader) {
  name_=(name.getOrElse(Nameable.NoName))
  override lazy val parent: StaticThreeDObject = initialParent.getOrElse(new StaticThreeDObject(Some(scene.staticObjects), name))

  parent.representations.add(this)
}
