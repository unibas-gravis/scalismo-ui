package org.statismo.stk.ui

import java.io.File
import java.io.IOException

import scala.util.Failure
import scala.util.Try

object SceneTreeObjectFactory {
  def combineFileExtensions(filters: Seq[SceneTreeObjectFactory[SceneTreeObject]]): Array[String] = {
    filters.map(_.ioMetadata.fileExtensions).flatten.toSeq.sorted.toArray
  }
  val DefaultFactories: Seq[SceneTreeObjectFactory[SceneTreeObject]] = Seq(ShapeModel, StaticMesh, StaticImage)

  def load(filename: String, factories: Seq[SceneTreeObjectFactory[SceneTreeObject]] = DefaultFactories)(implicit scene: Scene): Try[SceneTreeObject] = {
    val candidates = factories.filter(_.canPotentiallyHandleFile(filename))
    val file = new File(filename)
    val errors = candidates map ({ f =>
      val so = f.apply(file)
      if (so.isSuccess) {
        return so
      }
      so
    })
    val allErrors = errors.map(f => f match { case Failure(ex) => ex.getMessage; case _ => "" }).mkString
    Failure(new IOException(allErrors))
  }
}

trait SceneTreeObjectFactory[+T <: SceneTreeObject] {
  def ioMetadata: FileIoMetadata
  def canPotentiallyHandleFile(filename: String): Boolean = {
    val lc = filename.toLowerCase()
    ioMetadata.fileExtensions.map(ext => lc.endsWith("." + ext.toLowerCase())).filter(_ == true).size != 0
  }

  def apply(filename: String)(implicit scene: Scene): Try[T] = {
    apply(new File(filename))
  }

  def apply(file: File)(implicit scene: Scene): Try[T]
}

