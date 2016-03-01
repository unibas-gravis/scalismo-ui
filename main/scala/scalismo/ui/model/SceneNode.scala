package scalismo.ui.model

import scalismo.ui.event.ScalismoPublisher

import scala.collection.immutable

trait SceneNode extends ScalismoPublisher {
  def name: String

  def parent: SceneNode

  lazy val scene: Scene = parent.scene

  def children: immutable.Seq[SceneNode] = Nil

  override def toString: String = name
}

