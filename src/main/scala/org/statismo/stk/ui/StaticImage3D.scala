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
import scala.collection.immutable

object StaticImage3D extends SceneTreeObjectFactory[StaticImage3D[_]] with FileIoMetadata {
  override val description = "Static 3D Image"
  override val fileExtensions = immutable.Seq("nii", "nia")
  protected[ui] override val ioMetadata = this

  protected[ui] override def tryCreate(file: File)(implicit scene: Scene): Try[StaticImage3D[_]] = {
    createFromFile(file, None, file.getName)
  }

  def createFromFile(file: File, parent: Option[ThreeDObject], name: String)(implicit scene: Scene): Try[StaticImage3D[_]] = {
    {
      // Short
      val peerTry = ImageIO.read3DScalarImage[Short](file)
      if (peerTry.isSuccess) {
        return Success(new StaticImage3D(peerTry.get, parent, Some(name)))
      }
    }
    {
      // Float
      val peerTry = ImageIO.read3DScalarImage[Float](file)
      if (peerTry.isSuccess) {
        return Success(new StaticImage3D(peerTry.get, parent, Some(name)))
      }
    }
    {
      // Double
      val peerTry = ImageIO.read3DScalarImage[Double](file)
      if (peerTry.isSuccess) {
        return Success(new StaticImage3D(peerTry.get, parent, Some(name)))
      }
    }
    Failure(new IllegalArgumentException("could not load "+file.getAbsoluteFile))
  }

  def createFromPeer[S: ScalarValue: ClassTag : TypeTag](peer: DiscreteScalarImage3D[S], parent: Option[ThreeDObject] = None, name: Option[String] = None)(implicit scene: Scene): StaticImage3D[S] = {
    new StaticImage3D(peer, parent, name)
  }
}

class StaticImage3D[S: ScalarValue: ClassTag : TypeTag] protected[ui] (override val peer: DiscreteScalarImage3D[S], initialParent: Option[ThreeDObject] = None, name: Option[String] = None)(implicit override val scene: Scene) extends Image3D[S](peer) {
  name_=(name.getOrElse(Nameable.NoName))
  override lazy val parent: ThreeDObject = initialParent.getOrElse(new StaticThreeDObject(Some(scene.staticObjects), name))

  parent.representations.add(this)
}