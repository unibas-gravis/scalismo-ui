package scalismo.ui.model

import java.io.File

import scalismo.common.Scalar
import scalismo.geometry._3D
import scalismo.image.DiscreteScalarImage
import scalismo.io.ImageIO
import scalismo.io.ImageIO.ScalarType
import scalismo.ui.model.capabilities._
import scalismo.ui.model.properties._
import scalismo.ui.util.{ FileIoMetadata, FileUtil }
import spire.math.{ UByte, UInt, UShort }

import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag
import scala.util.{ Failure, Success, Try }

class ImagesNode(override val parent: GroupNode) extends SceneNodeCollection[ImageNode] with Loadable {
  override val name: String = "Images"

  def add(image: DiscreteScalarImage[_3D, Float], name: String): ImageNode = {
    val node = new ImageNode(this, image, name)
    add(node)
    node
  }

  override def loadMetadata: FileIoMetadata = FileIoMetadata.Image

  override def load(file: File): Try[Unit] = {
    read3DScalarImageAsType[Float](file) match {
      case Success(image) =>
        add(image, FileUtil.basename(file))
        Success(())
      case Failure(ex) => Failure(ex)
    }
  }

  // this method will be obsolete once the corresponding version is merged into scalismo core
  def read3DScalarImageAsType[S: Scalar: TypeTag: ClassTag](file: File, resampleOblique: Boolean = false, favourQform: Boolean = false): Try[DiscreteScalarImage[_3D, S]] = {
    def loadAs[T: Scalar: TypeTag: ClassTag]: Try[DiscreteScalarImage[_3D, T]] = {
      ImageIO.read3DScalarImage[T](file)
    }

    val result = (for {
      fileScalarType <- ScalarType.ofFile(file)
    } yield {
      val expectedScalarType = ScalarType.fromType[S]
      if (expectedScalarType == fileScalarType) {
        loadAs[S]
      } else {
        val s = implicitly[Scalar[S]]
        fileScalarType match {
          case ScalarType.Byte => loadAs[Byte].map(_.map(s.fromByte))
          case ScalarType.Short => loadAs[Short].map(_.map(s.fromShort))
          case ScalarType.Int => loadAs[Int].map(_.map(s.fromInt))
          case ScalarType.Float => loadAs[Float].map(_.map(s.fromFloat))
          case ScalarType.Double => loadAs[Double].map(_.map(s.fromDouble))
          case ScalarType.UByte => loadAs[UByte].map(_.map(u => s.fromShort(u.toShort)))
          case ScalarType.UShort => loadAs[UShort].map(_.map(u => s.fromInt(u.toInt)))
          case ScalarType.UInt => loadAs[UInt].map(_.map(u => s.fromLong(u.toLong)))

          case _ => Failure(new IllegalArgumentException(s"unknown scalar type $fileScalarType"))
        }
      }
    }).flatten
    result
  }

}

class ImageNode(override val parent: ImagesNode, val source: DiscreteScalarImage[_3D, Float], initialName: String) extends RenderableSceneNode with Grouped with Renameable with Removeable with HasWindowLevel with HasOpacity {
  name = initialName

  val (minimumValue, maximumValue) = {
    // we manually do this instead of using the min or max methods of the iterator
    // so that we only have to run through the list once.
    var min: Float = Float.MaxValue
    var max: Float = Float.MinValue
    source.values.foreach { value =>
      min = Math.min(min, value)
      max = Math.max(max, value)
    }
    (min, max)
  }

  override val windowLevel: WindowLevelProperty = {
    val range = maximumValue - minimumValue
    val window = range / 4
    val level = minimumValue + range / 2
    new WindowLevelProperty(WindowLevel(window, level))
  }

  override val opacity: OpacityProperty = new OpacityProperty()

  override def group: GroupNode = parent.parent

  override def remove(): Unit = parent.remove(this)
}