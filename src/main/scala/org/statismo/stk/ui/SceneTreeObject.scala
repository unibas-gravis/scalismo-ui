package org.statismo.stk.ui

import scala.swing.event.Event

object SceneTreeObject {
  case class ChildrenChanged(source: SceneTreeObject) extends Event
}

trait SceneTreeObject extends Nameable {
  lazy val parent: SceneTreeObject = ??? //you MUST override this. The exception that is thrown if you don't is intentional.
  def children: Seq[SceneTreeObject] = Nil

  def scene: Scene = {
    if (parent.isInstanceOf[Scene]) parent.asInstanceOf[Scene]
    else parent.scene
  }
  scene.listenTo(this)

  def displayables: List[Displayable] = {
    val cd = List(children.map(_.displayables).flatten).flatten
    if (this.isInstanceOf[Displayable]) {
      this.asInstanceOf[Displayable] :: cd
    } else cd
  }
}
