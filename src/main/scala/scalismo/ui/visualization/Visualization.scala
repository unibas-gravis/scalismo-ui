package scalismo.ui.visualization

import scalismo.ui._

import scala.collection.mutable

trait Visualizable[X <: Visualizable[X]] {
  protected[ui] def isVisibleIn(viewport: Viewport): Boolean

  def visualizationStrategy: VisualizationStrategy[X]
}

trait VisualizableSceneTreeObject[X <: VisualizableSceneTreeObject[X]] extends SceneTreeObject with Visualizable[X] {
  protected[ui] override def isVisibleIn(viewport: Viewport): Boolean = viewportVisibility(viewport)

}

trait VisualizationStrategy[V] {

  private class Instance[VI] {
    private val viewports = new mutable.WeakHashMap[Viewport, Seq[Renderable]]

    def apply(viewport: Viewport, create: => Seq[Renderable]) = {
      viewports.getOrElseUpdate(viewport, create)
    }
  }

  private val instances = new mutable.WeakHashMap[V, Instance[V]]

  protected[ui] final def applyUntyped(targetObject: Any, targetViewport: Viewport): Seq[Renderable] = {
    val typed = targetObject.asInstanceOf[V]
    apply(typed, targetViewport)
  }

  final def apply(target: V, viewport: Viewport): Seq[Renderable] = {
    val instance = instances.getOrElseUpdate(target, new Instance)
    instance.apply(viewport, renderablesFor(target, viewport))
  }

  private def renderablesFor(target: V, viewport: Viewport): Seq[Renderable] = {
    viewport match {
      case vp2: TwoDViewport => renderablesFor2D(target)
      case vp3: ThreeDViewport => renderablesFor3D(target)
    }
  }

  def renderablesFor2D(targetObject: V): Seq[Renderable]

  def renderablesFor3D(targetObject: V): Seq[Renderable]

}
