package org.statismo.stk.ui

import java.io.File

import scala.util.{Failure, Success, Try}

import org.statismo.stk.core.geometry.Point3D
import org.statismo.stk.core.image.DiscreteScalarImage3D
import org.statismo.stk.core.io.ImageIO
import org.statismo.stk.core.common.ScalarValue
import scala.reflect.ClassTag
import reflect.runtime.universe.TypeTag
import org.statismo.stk.ui.visualization.SimpleVisualizationFactory

object StaticImage3D extends SceneTreeObjectFactory[StaticImage3D[_]] with FileIoMetadata {
  val description = "Static 3D Image"
  val fileExtensions = Seq("nii", "nia")
  val ioMetadata = this

  def apply(file: File)(implicit scene: Scene): Try[StaticImage3D[_]] = {
    apply(file, None, file.getName)
  }

  def apply(file: File, parent: Option[ThreeDObject], name: String)(implicit scene: Scene): Try[StaticImage3D[_]] = {
    {
      // Short
      val peerTry = ImageIO.read3DScalarImage[Short](file)
      if (peerTry.isSuccess) {
        return Success(new StaticImage3D(peerTry.get, parent, Some(name)))
      }
    }
    {
      // Float
      val peerTry = ImageIO.read3DScalarImage[Short](file)
      if (peerTry.isSuccess) {
        return Success(new StaticImage3D(peerTry.get, parent, Some(name)))
      }
    }
    {
      // Double
      val peerTry = ImageIO.read3DScalarImage[Short](file)
      if (peerTry.isSuccess) {
        return Success(new StaticImage3D(peerTry.get, parent, Some(name)))
      }
    }
    Failure(new IllegalArgumentException("could not load "+file.getAbsoluteFile))
  }
}

class StaticImage3D[S: ScalarValue: ClassTag : TypeTag](override val peer: DiscreteScalarImage3D[S], initialParent: Option[ThreeDObject] = None, name: Option[String] = None)(implicit override val scene: Scene) extends Image3D[S](peer) {
  name_=(name.getOrElse(Nameable.NoName))
  override lazy val parent: ThreeDObject = initialParent.getOrElse(new StaticThreeDObject(Some(scene.staticObjects), name))

  parent.representations.add(this)
}