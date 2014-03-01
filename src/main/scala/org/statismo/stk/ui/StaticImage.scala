package org.statismo.stk.ui

import java.io.File

import scala.util.Try

import org.statismo.stk.core.geometry.Point3D
import org.statismo.stk.core.image.DiscreteScalarImage3D
import org.statismo.stk.core.io.ImageIO
import org.statismo.stk.core.common.ScalarValue
import scala.reflect.ClassTag
import reflect.runtime.universe.TypeTag

object StaticImage extends SceneTreeObjectFactory[StaticImage[_]] with FileIoMetadata {
  val description = "Static 3D Image"
  val fileExtensions = Seq("nii", "nia")
  val ioMetadata = this

  def apply(file: File)(implicit scene: Scene): Try[StaticImage[_]] = {
    apply(file, None, file.getName)
  }

  def apply(file: File, parent: Option[ThreeDObject], name: String)(implicit scene: Scene): Try[StaticImage[_]] = {
    for {
      raw <- ImageIO.read3DScalarImage[Short](file)
    } yield {
      new StaticImage(raw, parent, Some(name))
    }
  }
}

class StaticImage[A: ScalarValue : TypeTag : ClassTag](override val peer: DiscreteScalarImage3D[A], initialParent: Option[ThreeDObject] = None, name: Option[String] = None)(implicit override val scene: Scene) extends ThreeDImage[A] {
  name_=(name.getOrElse(Nameable.NoName))
  override lazy val parent: ThreeDObject = initialParent.getOrElse(new StaticThreeDObject(Some(scene.staticObjects), name))

  def addLandmarkAt(point: Point3D) = {
    parent.landmarks.addAt(point)
  }

  parent.representations.add(this)
}