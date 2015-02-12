package scalismo.ui

import java.io.File

import scalismo.geometry._3D
import scalismo.image.DiscreteScalarImage
import scalismo.io.ImageIO
import scalismo.ui.Reloadable.{ FileReloader, ImmutableReloader, Reloader }
import spire.math.Numeric

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
    {
      // Short
      val reloaderTry = Try {
        new FileReloader[DiscreteScalarImage[_3D, Short]](file) {
          override def doLoad() = ImageIO.read3DScalarImage[Short](file)
        }
      }
      if (reloaderTry.isSuccess) {
        return Success(new StaticImage3D(reloaderTry.get, parent, Some(name)))
      }
    }
    {
      // Float
      val reloaderTry = Try {
        new FileReloader[DiscreteScalarImage[_3D, Float]](file) {
          override def doLoad() = ImageIO.read3DScalarImage[Float](file)
        }
      }
      if (reloaderTry.isSuccess) {
        return Success(new StaticImage3D(reloaderTry.get, parent, Some(name)))
      }
    }
    {
      // Double
      val reloaderTry = Try {
        new FileReloader[DiscreteScalarImage[_3D, Double]](file) {
          override def doLoad() = ImageIO.read3DScalarImage[Double](file)
        }
      }
      if (reloaderTry.isSuccess) {
        return Success(new StaticImage3D(reloaderTry.get, parent, Some(name)))
      }
    }
    Failure(new IllegalArgumentException("could not load " + file.getAbsoluteFile))
  }

  def createFromPeer[S: Numeric: ClassTag: TypeTag](peer: DiscreteScalarImage[_3D, S], parent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit scene: Scene): StaticImage3D[S] = {
    new StaticImage3D(new ImmutableReloader[DiscreteScalarImage[_3D, S]](peer), parent, name)
  }
}

class StaticImage3D[S: Numeric: ClassTag: TypeTag] private[StaticImage3D] (reloader: Reloader[DiscreteScalarImage[_3D, S]], initialParent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit override val scene: Scene) extends Image3D[S](reloader) {
  name_=(name.getOrElse(Nameable.NoName))
  override lazy val parent: StaticThreeDObject = initialParent.getOrElse(new StaticThreeDObject(Some(scene.staticObjects), name))

  parent.representations.add(this)
}