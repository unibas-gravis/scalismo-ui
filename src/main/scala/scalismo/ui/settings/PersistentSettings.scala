package scalismo.ui.settings

import scala.reflect.runtime.universe.{ TypeTag, typeOf }
import scala.util.{ Failure, Success, Try }

object PersistentSettings {

  object Keys {
    final val WindowHeight = "common.windowHeight"
    final val WindowWidth = "common.windowWidth"
    final val WindowMaximized = "common.windowMaximized"
    final val LastUsedDirectories = "common.lastUsedDirectories"
    final val PerspectiveName = "common.perspective"
    final val ImageWindowLevelWindow = "common.image.windowlevel.Window"
    final val ImageWindowLevelLevel = "common.image.windowlevel.Level"
    final val SlicesVisible = "common.slices.visible"
    final val SlicesOpacity = "common.slices.opacity"
  }

  val settingsFile = {
    new ScalismoSettingsFile
  }

  class KeyDoesNotExistException extends Exception("Key not found")

  val KeyDoesNotExist = new KeyDoesNotExistException

  object Codecs {

    def get[A: TypeTag]: Codec[A] = {
      val codec: Option[Codec[_]] = typeOf[A] match {
        case t if t <:< typeOf[String] => Some(StringCodec)
        case t if t <:< typeOf[Int] => Some(IntCodec)
        case t if t <:< typeOf[Long] => Some(LongCodec)
        case t if t <:< typeOf[Short] => Some(ShortCodec)
        case t if t <:< typeOf[Byte] => Some(ByteCodec)
        case t if t <:< typeOf[Double] => Some(DoubleCodec)
        case t if t <:< typeOf[Float] => Some(FloatCodec)
        case t if t <:< typeOf[Boolean] => Some(BooleanCodec)
        case _ => None
      }
      if (codec.isEmpty) {
        throw new IllegalArgumentException("Settings of type " + typeOf[A].toString + " are not currently supported")
      }
      codec.get.asInstanceOf[Codec[A]]
    }

    abstract class Codec[A] {
      def toString(target: A): String = target.toString

      def fromString(s: String): A
    }

    object StringCodec extends Codec[String] {
      override def fromString(s: String) = s
    }

    object BooleanCodec extends Codec[Boolean] {
      override def fromString(s: String) = java.lang.Boolean.parseBoolean(s)
    }

    object IntCodec extends Codec[Int] {
      override def fromString(s: String) = Integer.parseInt(s)
    }

    object LongCodec extends Codec[Long] {
      override def fromString(s: String) = java.lang.Long.parseLong(s)
    }

    object ShortCodec extends Codec[Short] {
      override def fromString(s: String) = java.lang.Short.parseShort(s)
    }

    object ByteCodec extends Codec[Byte] {
      override def fromString(s: String) = java.lang.Byte.parseByte(s)
    }

    object DoubleCodec extends Codec[Double] {
      override def fromString(s: String) = java.lang.Double.parseDouble(s)
    }

    object FloatCodec extends Codec[Float] {
      override def fromString(s: String) = java.lang.Float.parseFloat(s)
    }

  }

  def get[A: TypeTag](key: String, default: Option[A] = None, useDefaultOnFailure: Boolean = false): Try[A] = {
    typeOf[A] match {
      case t if t <:< typeOf[Seq[_]] => throw new IllegalArgumentException("Use the getList() method to retrieve multi-valued settings.")
      case _ => /* ok */
    }

    val result = getList(key, default.map(List(_)), useDefaultOnFailure)
    result match {
      case Failure(error) => Failure(error)
      case Success(l) => Success(l.head)
    }
  }

  def getList[A: TypeTag](key: String, default: Option[List[A]] = None, useDefaultOnFailure: Boolean = false): Try[List[A]] = {
    require(!useDefaultOnFailure || default.isDefined, "useDefaultOnFailure requires default value to be set")

    val codec = Codecs.get[A]

    Try(doGet(key)) match {
      case Failure(error) => if (useDefaultOnFailure) Success(default.get) else Failure(error)
      case ok @ Success(r) =>
        if (r.isEmpty) {
          if (default.isDefined) Success(default.get)
          else Failure(KeyDoesNotExist)
        } else {
          Success(r.map(s => codec.fromString(s)).toList)
        }
    }
  }

  def set[A: TypeTag](key: String, value: A): Try[Unit] = {
    typeOf[A] match {
      case t if t <:< typeOf[Seq[_]] => throw new IllegalArgumentException("Use the setList() method to set multi-valued settings.")
      case _ => /* ok */
    }
    setList(key, List(value))
  }

  def setList[A: TypeTag](key: String, values: List[A]): Try[Unit] = {
    val codec = Codecs.get[A]

    Try(doSet(key, values.map(v => codec.toString(v)))) match {
      case ok @ Success(r) => Success(())
      case Failure(oops) => Failure(oops)
    }
  }

  private def doGet(key: String): Seq[String] = {
    val sf: SettingsFile = settingsFile
    sf.getValues(key)
  }

  private def doSet(key: String, vals: List[String]) = {
    val sf: SettingsFile = settingsFile
    sf.setValues(key, vals)
  }
}
