package org.statismo.stk.ui

import scala.swing.event.Event
import scala.util.Try
import scala.reflect.ClassTag
import org.statismo.stk.ui.visualization.Visualizable
import scala.collection.mutable

object SceneTreeObject {

  case class ChildrenChanged(source: SceneTreeObject) extends Event

  case class VisibilityChanged(source: SceneTreeObject) extends Event

}

trait SceneTreeObject extends Nameable {
  //you MUST override this. The exception that is thrown if you don't is intentional.
  def parent: SceneTreeObject = ???

  def children: Seq[SceneTreeObject] = Nil

  def scene: Scene = {
    parent match {
      case scene: Scene => scene
      case _ => parent.scene
    }
  }

  scene.listenTo(this)

  def visualizables(filter: Visualizable[_] => Boolean = {o => true}): Seq[Visualizable[_]] = find[Visualizable[_]](filter)

  reactions += {
    case Removeable.Removed(o) => if (o eq this) destroy()
  }

  def destroy() {
    children.foreach(_.destroy())
    // make sure that we don't leak memory...
    scene.deafTo(this)
  }

  def find[A <: AnyRef : ClassTag](filter: A => Boolean = {
    o: A => true
  }, maxDepth: Option[Int] = None, minDepth: Int = 1): Seq[A] = doFind(filter, maxDepth, minDepth, 0)

  private def doFind[A <: AnyRef : ClassTag](filter: A => Boolean, maxDepth: Option[Int], minDepth: Int, curDepth: Int): Seq[A] = {
    if (maxDepth.isDefined && maxDepth.get > curDepth) {
      Nil
    } else {
      val tail = children.map({
        c =>
          c.doFind[A](filter, maxDepth, minDepth, curDepth + 1)
      }).flatten
      val clazz = implicitly[ClassTag[A]].runtimeClass
      val head: Seq[A] = if (curDepth >= minDepth && clazz.isInstance(this)) {
        val candidate = this.asInstanceOf[A]
        if (filter(candidate)) Seq(candidate) else Nil
      } else {
        Nil
      }
      Seq(head, tail).flatten
    }
  }

  def onViewportsChanged(viewports: Seq[Viewport]): Unit = {
    Try {
      children foreach (_.onViewportsChanged(viewports))
    }
  }

  val visible = new Visibility(this)
}

class Visibility(container: SceneTreeObject) {
  val map = new mutable.WeakHashMap[Viewport, Boolean]

  def apply(viewport: Viewport): Boolean = map.getOrElse(viewport, true)

  def update(viewport: Viewport, nv: Boolean): Unit = update(viewport, nv, true)

  private def update(viewport: Viewport, nv: Boolean, isTop: Boolean): Boolean = {
    val selfChanged = if (apply(viewport) != nv) {
      map(viewport) = nv
      true
    } else false
    val notify = container.children.foldLeft(selfChanged)({case (b,c) => c.visible.update(viewport, nv, false) || false})
    if (isTop && notify) {
      container.scene.publish(new Scene.VisibilityChanged(container.scene))
    }
    notify
  }
}

