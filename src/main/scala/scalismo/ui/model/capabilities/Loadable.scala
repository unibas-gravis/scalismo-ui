package scalismo.ui.model.capabilities

import java.io.File

import scalismo.ui.model.SceneNode
import scalismo.ui.util.FileIoMetadata

import scala.util.Try

trait Loadable extends SceneNode {
  def loadMetadata: FileIoMetadata

  def load(file: File): Try[Unit]
}
