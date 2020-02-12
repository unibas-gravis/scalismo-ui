/*
 * Copyright (C) 2016  University of Basel, Graphics and Vision Research Group
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scalismo.ui.settings

import java.io.{File, PrintWriter}

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

  private def writeFile(values: List[String]): Unit = {
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
    val newLines = oldLines
      .filter({
        case (l, p) =>
          if (l.startsWith(prefix)) {
            if (position.isEmpty) {
              position = Some(p)
            }
            false
          } else {
            true
          }
      })
      .map(_._1)
      .zipWithIndex
    val (before, after) = position match {
      case Some(index) => (newLines.filter(_._2 < index).map(_._1), newLines.filter(_._2 >= index).map(_._1))
      case None        => (newLines.map(_._1), Nil)
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
    def toString(value: A): String

    def fromString(s: String): A
  }

  object Codec {

    /* Creates a codec using the default toString method to encode,
     * and a given function to decode.
     */
    def apply[A](decode: String => A): Codec[A] = {
      new Codec[A] {
        override def toString(value: A): String = value.toString

        override def fromString(s: String): A = decode(s)
      }
    }

    implicit val stringCodec: Codec[String] = Codec({ s: String =>
      s
    })

    implicit val booleanCodec: Codec[Boolean] = Codec(java.lang.Boolean.parseBoolean)

    implicit val intCodec: Codec[Int] = Codec(Integer.parseInt)

    implicit val longCodec: Codec[Long] = Codec(java.lang.Long.parseLong)

    implicit val shortCodec: Codec[Short] = Codec(java.lang.Short.parseShort)

    implicit val byteCodec: Codec[Byte] = Codec(java.lang.Byte.parseByte)

    implicit val doubleCodec: Codec[Double] = Codec(java.lang.Double.parseDouble)

    implicit val floatCodec: Codec[Float] = Codec(java.lang.Float.parseFloat)
  }

}
