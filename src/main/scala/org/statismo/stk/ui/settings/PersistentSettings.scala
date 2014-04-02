package org.statismo.stk.ui.settings

import reflect.runtime.universe.{TypeTag, typeOf}
import scala.util.{Failure, Success, Try}
import scala.None

object PersistentSettings {

  val settingsFile = {
    new StatismoSettingsFile
  }

  class KeyDoesNotExistException extends Exception("Key not found")
  private val KeyDoesNotExist = new KeyDoesNotExistException

  object Codecs {

    abstract class Codec[A] {
      def toString(target: A) : String = target.toString
      def fromString(s: String) : A
    }

    object StringCodec extends Codec[String] {
      override def fromString(s: String) = s
    }

    // FIXME: provide codecs for byte, short, long etc.
    object IntCodec extends Codec[Int] {
      override def fromString(s: String) = Integer.parseInt(s)
    }
  }
  import Codecs._

  def get[A: TypeTag](key: String, default: Option[A] = None, useDefaultOnFailure: Boolean = false): Try[A] = {
    typeOf[A] match {
      case t if t <:< typeOf[Seq[_]] => {
        throw new IllegalArgumentException("Use the getList() method to retrieve multi-valued settings.")
      }
      case _ => /* ok */
    }

    val result = getList(key, default.map(List(_)), useDefaultOnFailure)
    result match {
      case Success(l) => {
        if (l.isEmpty) {
          if (default.isDefined) Success(default.get)
          else Failure(KeyDoesNotExist)
        }
        else Success(l.head)
      }
      case Failure(error) => Failure(error)
    }
  }

  def getList[A: TypeTag](key: String, default: Option[Seq[A]] = None, useDefaultOnFailure: Boolean = false): Try[List[A]] = {
    require(!useDefaultOnFailure || default.isDefined, "useDefaultOnFailure requires default value to be set")

    val codec: Option[Codec[_]] = typeOf[A] match {
      case t if t <:< typeOf[String] => Some(StringCodec)
      case t if t <:< typeOf[Int] => Some(IntCodec)
      case _ => None
    }
    if (!codec.isDefined) {
      throw new IllegalArgumentException("Settings of type " + typeOf[A].toString +" are not currently supported")
    }

    Try(tryGet(key)) match {
      case ok@Success(r) => {
        Success(r.map(s => codec.get.asInstanceOf[Codec[A]].fromString(s)).toList)
      }
      case Failure(oops) => Failure(oops)
    }
  }

  private def tryGet(key: String) : Seq[String] = {
    val sf: SettingsFile = settingsFile
    val vals:Seq[String] = sf.getValues(key)
    vals
  }
}
