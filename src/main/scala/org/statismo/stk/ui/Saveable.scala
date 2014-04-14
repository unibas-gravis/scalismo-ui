package org.statismo.stk.ui

import java.io.File

import scala.util.Try

trait Saveable {
  protected[ui] def saveableMetadata: FileIoMetadata

  def saveToFile(file: File): Try[Unit]

  protected[ui] def isCurrentlySaveable: Boolean = true
}