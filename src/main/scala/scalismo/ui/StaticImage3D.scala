package scalismo.ui

import java.io.File

import scalismo.common.Scalar
import scalismo.geometry._3D
import scalismo.image.DiscreteScalarImage
import scalismo.io.ImageIO
import scalismo.io.ImageIO.ScalarType
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

    def loadAs[T: Scalar: TypeTag: ClassTag]: Try[StaticImage3D[T]] = {
      val reloaderTry = Try {
        new FileReloader[DiscreteScalarImage[_3D, T]](file) {
          override def doLoad() = ImageIO.read3DScalarImage[T](file)
        }
      }
      reloaderTry.map(r => new StaticImage3D(r, parent, Some(name)))
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

  def createFromPeer[S: Scalar: ClassTag: TypeTag](peer: DiscreteScalarImage[_3D, S], parent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit scene: Scene): StaticImage3D[S] = {
    new StaticImage3D(new ImmutableReloader[DiscreteScalarImage[_3D, S]](peer), parent, name)
  }
}

class StaticImage3D[S: Scalar: ClassTag: TypeTag] private[StaticImage3D] (reloader: Reloader[DiscreteScalarImage[_3D, S]], initialParent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit override val scene: Scene) extends Image3D[S](reloader) {
  name_=(name.getOrElse(Nameable.NoName))
  override lazy val parent: StaticThreeDObject = initialParent.getOrElse(new StaticThreeDObject(Some(scene.staticObjects), name))

  parent.representations.add(this)
}