package scalismo.ui.settings

import java.io.{ File, PrintWriter }

import scala.io.Codec

/**
 * An ini-like file containing key/value setting pairs.
 * The methods here may throw runtime exceptions.
 *
 * @see [[GlobalSettings]]
 */
class SettingsFile(directory: File, name: String) {
  private val file = new File(directory.getAbsolutePath + File.separator + name)

  private def readFile(): List[String] = {
    if (file.isFile) {
      val s = scala.io.Source.fromFile(file)(codec = Codec.UTF8)
      val r = s.getLines().toList
      s.close()
      r
    } else Nil
  }

  private def writeFile(values: List[String]) = {
    if (!file.exists) {
      if (!file.getParentFile.exists) {
        file.getParentFile.mkdirs()
      }
    }
    val writer = new PrintWriter(file, "UTF8")
    values.foreach(writer.println)
    writer.close()
  }

  def getValues(key: String): List[String] = {
    val lines = readFile()
    val prefix = s"$key="
    val prefixLength = prefix.length
    lines.filter(_.startsWith(prefix)).map(l => l.substring(prefixLength))
  }

  def setValues(key: String, vals: List[String]): Unit = {
    val oldLines = readFile().zipWithIndex
    var position: Option[Int] = None
    val prefix = s"$key="
    val newLines = oldLines.filter({
      case (l, p) =>
        if (l.startsWith(prefix)) {
          if (position.isEmpty) {
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
    val all = List(before, inserted, after).flatten
    writeFile(all)
  }
}

object SettingsFile {
  /**
   * Just a fancy name for serialization and deserialization
   * methods of a given type, preferrably to a human-readable form.
   *
   * @tparam A the type that can be (de)serialized
   */
  trait Codec[A] {
    def toString(target: A): String = target.toString

    def fromString(s: String): A
  }

  object Codec {

    implicit val stringCodec = new Codec[String] {
      override def fromString(s: String) = s
    }

    implicit val booleanCodec = new Codec[Boolean] {
      override def fromString(s: String) = java.lang.Boolean.parseBoolean(s)
    }

    implicit val intCodec = new Codec[Int] {
      override def fromString(s: String) = Integer.parseInt(s)
    }

    implicit val longCodec = new Codec[Long] {
      override def fromString(s: String) = java.lang.Long.parseLong(s)
    }

    implicit val shortCodec = new Codec[Short] {
      override def fromString(s: String) = java.lang.Short.parseShort(s)
    }

    implicit val byteCodec = new Codec[Byte] {
      override def fromString(s: String) = java.lang.Byte.parseByte(s)
    }

    implicit val doubleCodec = new Codec[Double] {
      override def fromString(s: String) = java.lang.Double.parseDouble(s)
    }

    implicit val floatCodec = new Codec[Float] {
      override def fromString(s: String) = java.lang.Float.parseFloat(s)
    }
  }
}