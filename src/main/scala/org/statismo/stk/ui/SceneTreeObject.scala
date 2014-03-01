package org.statismo.stk.ui

import scala.swing.event.Event
import scala.util.Try

object SceneTreeObject {
  case class ChildrenChanged(source: SceneTreeObject) extends Event
  case class VisibilityChanged(source: SceneTreeObject) extends Event
}

trait SceneTreeObject extends Nameable {
  def parent: SceneTreeObject = ??? //you MUST override this. The exception that is thrown if you don't is intentional.
  def children: Seq[SceneTreeObject] = Nil

  def scene: Scene = {
    parent match {
      case scene: Scene => scene
      case _ => parent.scene
    }
  }
  scene.listenTo(this)
  
  def displayables: List[Displayable] = {
    val childDisplayables = List(children.map(_.displayables).flatten).flatten
    this match {
      case displayable: Displayable =>
        displayable :: childDisplayables
      case _ => childDisplayables
    }
  }
  
  reactions += {
    case Removeable.Removed(o) => if (o eq this) destroy()
  }
  
  def destroy() {
    children.foreach(_.destroy())
    // make sure that we don't leak memory...
    scene.deafTo(this)
  }

  private var hidden: List[Viewport] = if (this.isInstanceOf[Scene]) Nil else scene.viewports.filterNot(v => v.supportsShowingObject(this)).toList

  def isHiddenInViewport(viewport: Viewport) = hidden.exists({ v => v eq viewport }) || !scene.viewports.exists({v => v eq viewport})
  def isShownInViewport(viewport: Viewport) = !isHiddenInViewport(viewport)

  private def hideInViewports(viewports: Seq[Viewport], notify: Boolean): Unit = this.synchronized {
    val changed = viewports.map { viewport =>
      if (isShownInViewport(viewport)) {
        hidden ::= viewport
        true
      } else false
    }.foldLeft(false) { case (a, b) => a || b }
    children foreach { c => c.hideInViewports(viewports, notify && !changed) }
    if (changed && notify) {
      publish(SceneTreeObject.VisibilityChanged(this))
    }
  }

  private def showInViewports(viewports: Seq[Viewport], notify: Boolean): Unit = this.synchronized {
    val changed = viewports.map { viewport =>
      if (isHiddenInViewport(viewport)) {
        hidden = hidden filterNot(_ eq viewport)
        true
      } else false
    }.foldLeft(false) { case (a, b) => a || b }

    children foreach { c => c.showInViewports(viewports, notify && !changed) }
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
    hidden = viewports.filterNot(v => v.supportsShowingObject(this)).toList
    Try {children foreach (_.onViewportsChanged(viewports))}
  }
}
