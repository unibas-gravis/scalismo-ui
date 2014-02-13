package org.statismo.stk.ui

import java.io.File

import scala.reflect.runtime.universe.TypeTag.Short
import scala.util.Try

import org.statismo.stk.core.geometry.Point3D
import org.statismo.stk.core.image.DiscreteScalarImage3D
import org.statismo.stk.core.io.ImageIO

object StaticImage extends SceneTreeObjectFactory[StaticImage] with FileIoMetadata {
  val description = "Static 3D Image"
  val fileExtensions = Seq("nii", "nia")
  val ioMetadata = this

  def apply(file: File)(implicit scene: Scene): Try[StaticImage] = {
    apply(file, None, file.getName())
  }

  def apply(file: File, parent: Option[ThreeDRepresentations], name: String)(implicit scene: Scene): Try[StaticImage] = {
    for {
      raw2 <- ImageIO.read3DScalarImage[Float](file)
      raw <- ImageIO.read3DScalarImage[Short](file)
    } yield {
      println(raw2)
      new StaticImage(raw, parent, Some(name))
    }
  }
}

class StaticImage(override val peer: DiscreteScalarImage3D[Short], initialParent: Option[ThreeDRepresentations] = None, name: Option[String] = None)(implicit override val scene: Scene) extends ThreeDImage {
  name_=(name.getOrElse(Nameable.NoName))

  override lazy val parent: ThreeDRepresentations = initialParent.getOrElse {
    val p = new StaticThreeDObject(Some(scene.staticObjects), name)
    p.representations
  }

  def addLandmarkAt(point: Point3D) = {
    val landmarks = parent.parent.landmarks
    landmarks.addAt(point)
  }

  parent.add(this)
}