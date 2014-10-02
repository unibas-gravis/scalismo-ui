package org.statismo.stk.ui

import java.io.File

import scala.util.{Failure, Success, Try}

trait Reloadable {
  def reload() : Try[Unit]
  def isCurrentlyReloadable: Boolean
}

object Reloadable {

  abstract class Reloader[T] {
    def isReloadable: Boolean
    def load(): Try[T]
  }

  abstract class FileReloader[T](file: File) extends Reloader[T] {
    override def isReloadable = true
  }

  class ImmutableReloader[T](value: T) extends Reloader[T] {
    override def isReloadable = false
    override val load = Success(value)
  }
}