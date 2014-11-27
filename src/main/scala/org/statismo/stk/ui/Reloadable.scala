package org.statismo.stk.ui

import java.io.File

import scala.util.{Success, Try}

trait Reloadable {
  def reload(): Try[Unit]
  def isCurrentlyReloadable: Boolean
}

object Reloadable {

  abstract class Reloader[T] {
    def isReloadable: Boolean

    protected[Reloader] def doLoad(): Try[T]

    /* Assumes that the first invocation of doLoad() never fails.
     * This is on purpose, to guarantee a "fail fast"
     * behavior. In other words, if the first call to
     * doLoad returns a Failure, then the instantiation
     * of the Reloader object as such will fail.
     */
    private var initialValue: T = doLoad().get

    final def load(): Try[T] = {
      // on first call, return initial value.
      if (initialValue != null) this.synchronized {
        if (initialValue != null) {
          val result = Success(initialValue)
          initialValue = null.asInstanceOf[T]
          return result
        }
      }
      // on all subsequent calls, delegate to actual method.
      doLoad()
    }
  }

  abstract class FileReloader[T](file: File) extends Reloader[T] {
    override def isReloadable = true
  }

  class ImmutableReloader[T](value: T) extends Reloader[T] {
    override def isReloadable = false
    override lazy val doLoad = Success(value)
  }
}