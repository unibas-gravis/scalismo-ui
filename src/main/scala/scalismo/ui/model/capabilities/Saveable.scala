package scalismo.ui.model.capabilities

import java.io.File

import scalismo.ui.model.SceneNode
import scalismo.ui.util.FileIoMetadata

import scala.util.Try

trait Saveable extends SceneNode {
  def saveMetadata: FileIoMetadata

  def save(file: File): Try[Unit]
}
