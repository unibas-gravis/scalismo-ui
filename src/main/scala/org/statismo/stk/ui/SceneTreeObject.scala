package org.statismo.stk.ui

import scala.swing.event.Event
import scala.util.Try
import scala.reflect.ClassTag
import org.statismo.stk.ui.visualization.Visualizable

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

  def visualizables: Seq[Visualizable[_]] = find[Visualizable[_]]()

  reactions += {
    case Removeable.Removed(o) => if (o eq this) destroy()
  }

  def destroy() {
    children.foreach(_.destroy())
    // make sure that we don't leak memory...
    scene.deafTo(this)
  }

  def find[A <: AnyRef : ClassTag](filter: A => Boolean = {o:A => true}, maxDepth: Option[Int] = None, minDepth: Int = 1): Seq[A] = doFind(filter, maxDepth, minDepth, 0)

  private def doFind[A <: AnyRef : ClassTag](filter: A => Boolean, maxDepth: Option[Int], minDepth: Int, curDepth: Int): Seq[A] = {
    if (maxDepth.isDefined && maxDepth.get > curDepth) {
      Nil
    } else {
      val tail = children.map({c=>
        c.doFind[A](filter, maxDepth, minDepth, curDepth + 1)
      }).flatten
      val clazz = implicitly[ClassTag[A]].runtimeClass
      val head: Seq[A] = if (curDepth >= minDepth && clazz.isInstance(this)) {
        val candidate = this.asInstanceOf[A]
        if (filter(candidate)) Seq(candidate) else Nil
      } else { Nil }
      Seq(head, tail).flatten
    }
  }

  private var hidden: List[Viewport] = Nil

  def isHiddenInViewport(viewport: Viewport) = hidden.exists({
    v => v eq viewport
  }) || !scene.viewports.exists({
    v => v eq viewport
  })

  def isShownInViewport(viewport: Viewport) = !isHiddenInViewport(viewport)

  private def hideInViewports(viewports: Seq[Viewport], notify: Boolean): Unit = this.synchronized {
    val changed = viewports.map {
      viewport =>
        if (isShownInViewport(viewport)) {
          hidden ::= viewport
          true
        } else false
    }.foldLeft(false) {
      case (a, b) => a || b
    }
    children foreach {
      c => c.hideInViewports(viewports, notify && !changed)
    }
    if (changed && notify) {
      publish(SceneTreeObject.VisibilityChanged(this))
    }
  }

  private def showInViewports(viewports: Seq[Viewport], notify: Boolean): Unit = this.synchronized {
    val changed = viewports.map {
      viewport =>
        if (isHiddenInViewport(viewport)) {
          hidden = hidden filterNot (_ eq viewport)
          true
        } else false
    }.foldLeft(false) {
      case (a, b) => a || b
    }

    children foreach {
      c => c.showInViewports(viewports, notify && !changed)
    }
    if (changed && notify) {
      publish(SceneTreeObject.VisibilityChanged(this))
    }
  }

  def showInAllViewports(): Unit = showInViewports(scene.viewports)

  def hideInAllViewports(): Unit = hideInViewports(scene.viewports)

  def hideInViewports(viewports: Seq[Viewport]): Unit = hideInViewports(viewports, notify = true)

  def showInViewports(viewports: Seq[Viewport]): Unit = showInViewports(viewports, notify = true)

  def hideInViewport(viewport: Viewport): Unit = hideInViewports(Seq(viewport))

  def showInViewport(viewport: Viewport): Unit = showInViewports(Seq(viewport))

  def onViewportsChanged(viewports: Seq[Viewport]): Unit = {
    Try {
      children foreach (_.onViewportsChanged(viewports))
    }
  }
}
