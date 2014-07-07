package org.statismo.stk.ui.settings

import java.io.{PrintWriter, File}
import scala.collection.immutable
import scala.io.Codec

object SettingsFile {

}

class StatismoSettingsFile extends SettingsFile(SettingsDirectory.get().get, "global.ini")

class SettingsFile(directory: File, name: String) {
  private val file = new File(directory.getAbsolutePath + File.separator + name)

  private def readFile(): Seq[String] = {
    if (file.isFile) {
      val s = scala.io.Source.fromFile(file)(codec = Codec.UTF8)
      val r = s.getLines().toList
      s.close()
      r
    } else Nil
  }

  private def writeFile(values: Seq[String]) = {
    if (!file.exists) {
      if (!file.getParentFile.exists) {
        file.getParentFile.mkdirs()
      }
    }
    val writer = new PrintWriter(file, "UTF8")
    values.foreach(writer.println)
    writer.close()
  }

  def getValues(key: String): Seq[String] = {
    val lines = readFile()
    val prefix = s"$key="
    val prefixLength = prefix.length
    lines.filter(_.startsWith(prefix)).map(l => l.substring(prefixLength))
  }

  def setValues(key: String, vals: immutable.Seq[String]): Unit = {
    val oldLines = readFile().zipWithIndex
    var position: Option[Int] = None
    val prefix = s"$key="
    val newLines = oldLines.filter({
      case (l, p) =>
        if (l.startsWith(prefix)) {
          if (position == None) {
            position = Some(p)
          }
          false
        } else {
          true
        }
    }).map(_._1).zipWithIndex
    val (before, after) = position match {
      case Some(index) => (newLines.filter(_._2 < index).map(_._1), newLines.filter(_._2 >= index).map(_._1))
      case None => (newLines.map(_._1), Nil)
    }
    val inserted = vals.map(s => s"$prefix$s")
    val all = Seq(before, inserted, after).flatten
    writeFile(all)
  }
}
