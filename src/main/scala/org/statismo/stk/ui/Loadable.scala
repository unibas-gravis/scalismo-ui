package org.statismo.stk.ui

import scala.util.Try
import java.io.File
import scala.util.Failure
import java.io.IOException

object Loadable {
  def combineFileExtensions(filters: Seq[Loadable[SceneObject]]) : Array[String] = {
    filters.map(_.fileExtensions).flatten.toSeq.sorted.toArray
  }
  val defaultFactories: Seq[Loadable[SceneObject]] = Seq(RawMesh, StatModel)
  
  def load(filename: String, factories: Seq[Loadable[SceneObject]] = defaultFactories): Try[SceneObject] = {
    val candidates = factories.filter(_.canPotentiallyHandleFile(filename))
    val file = new File(filename)
    val errors = candidates map ({ f =>
      val so = f.apply(file)
      if (so.isSuccess) {
        return so
      }
      so
    })
    val allErrors = errors.map(f => f match {case Failure(ex) => ex.getMessage; case _ => ""}).mkString
    Failure(new IOException(allErrors))
  }
}

trait Loadable[+T <: SceneObject] {
  val fileExtensions: Seq[String]
  val description: String
  
  def longDescription: String = {
    description + fileExtensions.mkString(" (*.", ", *.", ")")
  }
  
  def canPotentiallyHandleFile(filename: String): Boolean = {
    val lc = filename.toLowerCase()
    fileExtensions.map(ext => lc.endsWith("."+ext.toLowerCase())).filter(_ == true).size != 0
  }
  
  def apply(filename: String): Try[T] = {
    apply(new File(filename))
  }

  def apply(file: File): Try[T]
}

