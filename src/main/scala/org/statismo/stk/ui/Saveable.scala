package org.statismo.stk.ui

import java.io.File

import scala.util.Try

trait Saveable {
  def saveableMetadata: FileIoMetadata
  def saveToFile(file: File): Try[Unit]
  def isCurrentlySaveable: Boolean = true
}