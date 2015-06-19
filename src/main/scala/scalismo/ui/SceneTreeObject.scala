package scalismo.ui

import scalismo.ui.visualization.Visualizable

import scala.collection.mutable
import scala.reflect.ClassTag
import scala.swing.event.Event
import scala.util.Try

object SceneTreeObject {

  case class ChildrenChanged(source: SceneTreeObject) extends Event

  case class VisibilityChanged(source: SceneTreeObject) extends Event

  case class Destroyed(source: SceneTreeObject) extends Event

}

trait SceneTreeObject extends Nameable {
  //you MUST override this. The exception that is thrown if you don't is intentional.
  def parent: SceneTreeObject = throw new NotImplementedError

  protected[ui] def children: Seq[SceneTreeObject] = Nil

  def scene: Scene = {
    parent match {
      case scene: Scene => scene
      case _ => parent.scene
    }
  }

  scene.listenTo(this)

  protected[ui] def visualizables(filter: Visualizable[_] => Boolean = {
    o => true
  }): Seq[Visualizable[_]] = findAny[Visualizable[_]](filter, None, 1, 0)

  reactions += {
    case Removeable.Removed(o) => if (o eq this) {
      destroy()
    }
  }

  protected final def destroy() {
    children.foreach(_.destroy())
    publish(SceneTreeObject.Destroyed(this))
  }

  def find[A <: SceneTreeObject: ClassTag](filter: A => Boolean = {
    o: A => true
  }, maxDepth: Option[Int] = None, minDepth: Int = 1): Seq[A] = findAny(filter, maxDepth, minDepth, 0)

  private def findAny[A <: AnyRef: ClassTag](filter: A => Boolean, maxDepth: Option[Int], minDepth: Int, curDepth: Int): Seq[A] = {
    if (maxDepth.isDefined && maxDepth.get > curDepth) {
      Nil
    } else {
      val tail = children.map({
        c =>
          c.findAny[A](filter, maxDepth, minDepth, curDepth + 1)
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

  protected def onViewportsChanged(viewports: Seq[Viewport]): Unit = {
    Try {
      children foreach (_.onViewportsChanged(viewports))
    }
  }

  val viewportVisibility = new Visibility(this)

  // convenience methods
  def visible_=(newVisibility: Boolean) = {
    scene.perspective.viewports.foreach(vp => viewportVisibility.update(vp, newVisibility))
  }

  // returns true if and only if the object is visible in *all* viewports
  def visible: Boolean = {
    scene.perspective.viewports.forall(vp => viewportVisibility(vp))
  }
}

class Visibility(container: SceneTreeObject) {
  val map = new mutable.WeakHashMap[Viewport, Boolean]

  def apply(viewport: Viewport): Boolean = map.getOrElse(viewport, true)

  def update(viewport: Viewport, visible: Boolean): Unit = update(viewport, visible, isTop = true)

  private def update(viewport: Viewport, nv: Boolean, isTop: Boolean): Boolean = {
    val selfChanged = if (apply(viewport) != nv) {
      map(viewport) = nv
      true
    } else false
    val notify = container.children.foldLeft(selfChanged)({
      case (b, c) => c.viewportVisibility.update(viewport, nv, isTop = false) || b
    })
    if (isTop && notify) {
      container.scene.publishVisibilityChanged()
    }
    notify
  }

  //  initialize with parent visibility
  if (container ne container.parent) {
    for (v <- container.scene.viewports) {
      if (!container.parent.viewportVisibility(v)) {
        map(v) = false
      }
    }
  }
}

