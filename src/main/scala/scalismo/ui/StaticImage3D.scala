package scalismo.ui

import java.io.File

import scalismo.common.Scalar
import scalismo.geometry._3D
import scalismo.image.DiscreteScalarImage
import scalismo.io.ImageIO
import scalismo.ui.Reloadable.{ FileReloader, ImmutableReloader, Reloader }
import spire.math.{ ULong, UByte, UInt, UShort }

import scala.collection.immutable
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag
import scala.util.{ Failure, Success, Try }

object StaticImage3D extends SceneTreeObjectFactory[StaticImage3D[_]] with FileIoMetadata {
  override val description = "Static 3D Image"
  override val fileExtensions = immutable.Seq("nii", "nia", "vtk")
  protected[ui] override val ioMetadata = this

  protected[ui] override def tryCreate(file: File)(implicit scene: Scene): Try[StaticImage3D[_]] = {
    createFromFile(file, None, file.getName)
  }

  def createFromFile(file: File, parent: Option[StaticThreeDObject], name: String)(implicit scene: Scene): Try[StaticImage3D[_]] = {
    // We don't know what data type the image is, so we try them all. There should be a cleaner solution to this sooner or later
    // (like: asking the framework what the type is). For now, we just try the most commonly used types first...

    import scala.language.implicitConversions
    implicit def functionToPartialFunction[I, O](f: I => O): PartialFunction[I, O] = { case i => f(i) }

    def loadAs[T: Scalar: TypeTag: ClassTag](ignored: Throwable): Try[StaticImage3D[T]] = {
      val reloaderTry = Try {
        new FileReloader[DiscreteScalarImage[_3D, T]](file) {
          override def doLoad() = ImageIO.read3DScalarImage[T](file)
        }
      }
      reloaderTry.map(r => new StaticImage3D(r, parent, Some(name)))
    }

    // the order is somewhat arbitrary, but expected to be roughly in
    // descending order of "encountering such images in the wild"
    loadAs[Short](null)
      .recoverWith(loadAs[Float] _)
      .recoverWith(loadAs[Double] _)
      .recoverWith(loadAs[Int] _)
      .recoverWith(loadAs[Byte] _)
      .recoverWith(loadAs[Long] _)
      .recoverWith(loadAs[UShort] _)
      .recoverWith(loadAs[UInt] _)
      .recoverWith(loadAs[UByte] _)
      .recoverWith(loadAs[ULong] _)
  }

  def createFromPeer[S: Scalar: ClassTag: TypeTag](peer: DiscreteScalarImage[_3D, S], parent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit scene: Scene): StaticImage3D[S] = {
    new StaticImage3D(new ImmutableReloader[DiscreteScalarImage[_3D, S]](peer), parent, name)
  }
}

class StaticImage3D[S: Scalar: ClassTag: TypeTag] private[StaticImage3D] (reloader: Reloader[DiscreteScalarImage[_3D, S]], initialParent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit override val scene: Scene) extends Image3D[S](reloader) {
  name_=(name.getOrElse(Nameable.NoName))
  override lazy val parent: StaticThreeDObject = initialParent.getOrElse(new StaticThreeDObject(Some(scene.staticObjects), name))

  parent.representations.add(this)
}