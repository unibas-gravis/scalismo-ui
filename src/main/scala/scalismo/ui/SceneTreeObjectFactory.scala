package scalismo.ui

import java.io.{ File, IOException }

import scala.util.{ Failure, Try }

object SceneTreeObjectFactory {
  def combineFileExtensions(filters: Seq[SceneTreeObjectFactory[SceneTreeObject]]): Array[String] = {
    filters.map(_.ioMetadata.fileExtensions).flatten.toSeq.sorted.toArray
  }

  val DefaultFactories: Seq[SceneTreeObjectFactory[SceneTreeObject]] = Seq(ShapeModel, StaticMesh, StaticImage3D)

  def load(filename: String, factories: Seq[SceneTreeObjectFactory[SceneTreeObject]] = DefaultFactories)(implicit scene: Scene): Try[SceneTreeObject] = {
    val candidates = factories.filter(_.canPotentiallyHandleFile(filename))
    val file = new File(filename)
    val errors = candidates map {
      factory =>
        val outcome = factory.tryCreate(file)
        if (outcome.isSuccess) {
          return outcome
        }
        outcome
    }
    val allErrors = errors.map {
      case Failure(ex) => ex.getMessage
      case _ => ""
    }.mkString
    Failure(new IOException(allErrors))
  }
}

trait SceneTreeObjectFactory[+T <: SceneTreeObject] {
  protected[ui] def ioMetadata: FileIoMetadata

  protected[ui] def canPotentiallyHandleFile(filename: String): Boolean = {
    val lc = filename.toLowerCase
    ioMetadata.fileExtensions.map(ext => lc.endsWith("." + ext.toLowerCase)).count(_ == true) != 0
  }

  protected[ui] def tryCreate(file: File)(implicit scene: Scene): Try[T]
}

