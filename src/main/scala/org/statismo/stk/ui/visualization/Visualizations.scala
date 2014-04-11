package org.statismo.stk.ui.visualization

import org.statismo.stk.ui.{SceneTreeObject, EdtPublisher, Viewport}
import scala.util.{Failure, Success, Try}
import scala.ref.WeakReference
import scala.swing.event.Event
import scala.language.existentials
import scala.collection.{mutable, immutable}
import java.util.Date

class Visualizations {
  private type ViewportOrClassName = Either[Viewport, String]

  private val perviewport = new mutable.WeakHashMap[ViewportOrClassName, PerViewport]

  private class PerViewport(val context: ViewportOrClassName) {
    private val mappings = new mutable.WeakHashMap[VisualizationProvider[_], Try[Visualization[_]]]

    def tryGet(key: VisualizationProvider[_]) : Try[Visualization[_]] = {
      //FIXME too: this method is invoked far too often.
      //FIXME
      //System.gc()
      //println(new Date()+ " " +key.getClass + " " +key+" " + " map size = " + mappings.size)
      //mappings.keys.foreach { p => println(s"\t$p") }

      val value = mappings.getOrElseUpdate(key, {
        val existing: Try[Visualization[_]] = key match {
          case fac: VisualizationFactory[_] =>
            context match {
              case Left(viewport) => Visualizations.this.tryGet(key, viewport.getClass.getCanonicalName)
              case Right(vpClass) => Try{fac.instantiate(vpClass)}
            }
          case _ => tryGet(key.visualizationProvider)
        }
        existing match {
          case Success(ok) => Try{ok.derive()}
          case f@Failure(e) => f
        }
      })
      value.asInstanceOf[Try[Visualization[_]]]
    }
  }

  private def tryGet(key: VisualizationProvider[_], context: ViewportOrClassName): Try[Visualization[_]] = {
    val delegate = perviewport.getOrElseUpdate(context, new PerViewport(context))
    delegate.tryGet(key)
  }

  def tryGet (key: VisualizationProvider[_], context: Viewport): Try[Visualization[_]] = tryGet(key, Left(context))
  def tryGet (key: VisualizationProvider[_], context: String): Try[Visualization[_]] = tryGet(key, Right(context))
  def getUnsafe[R <: Visualization[_]] (key: VisualizationProvider[_], context: Viewport): R = tryGet(key, context).get.asInstanceOf[R]
  def getUnsafe[R <: Visualization[_]] (key: VisualizationProvider[_], context: String): R = tryGet(key, context).get.asInstanceOf[R]

}

trait VisualizationFactory[A <: Visualizable[_]] extends VisualizationProvider[A] {
  protected[ui] override final val visualizationProvider = null

  protected[ui] def visualizationsFor(viewportClassName: String): Seq[Visualization[A]]
  protected[ui] final def instantiate(viewportClassName: String): Visualization[A] = {
    visualizationsFor(viewportClassName).headOption match {
      case Some(v) => v
      case None => throw new RuntimeException(getClass+ " did not provide any Visualization options for viewport class "+viewportClassName)
    }
  }
}

trait SimpleVisualizationFactory[A <: Visualizable[_]] extends VisualizationFactory[A] {
  protected var visualizations = new immutable.HashMap[String, Seq[Visualization[A]]]
  final override def visualizationsFor(viewportClassName: String): Seq[Visualization[A]] = visualizations.getOrElse(viewportClassName, Nil)
}

trait VisualizationProvider[-A <: Visualizable[_]] {
  protected[ui] def visualizationProvider: VisualizationProvider[A]
}

trait Visualizable[X <: Visualizable[X]] extends VisualizationProvider[X] {
  protected[ui] def isVisibleIn(viewport: Viewport) : Boolean
}

trait VisualizableSceneTreeObject[X <: VisualizableSceneTreeObject[X]] extends SceneTreeObject with Visualizable[X] {
  protected[ui] override def isVisibleIn(viewport: Viewport) : Boolean = visible(viewport)
}

trait Derivable[A <: AnyRef] {
  protected val self:A = this.asInstanceOf[A]
  private var _derived: immutable.Seq[WeakReference[A]] = Nil

  protected [visualization] def derived: immutable.Seq[A] = derivedInUse.map(r => r.get).filter(o => o != None).map(o => o.get)

  private def derivedInUse: immutable.Seq[WeakReference[A]] = this.synchronized {
    _derived = _derived.filter(w => w.get != None)
    _derived
  }

  final def derive(): A = this.synchronized {
    val child: A = createDerived()
    _derived = Seq(derivedInUse, Seq(new WeakReference[A](child))).flatten.to[immutable.Seq]
    child
  }

  protected def createDerived() :A
}

trait Visualization[A <: Visualizable[_]] extends Derivable[Visualization[A]] {
  private val mappings = new mutable.WeakHashMap[A, Seq[Renderable]]
  final def apply(target: Visualizable[_]) = {
    val typed: A = target.asInstanceOf[A]
    mappings.getOrElseUpdate(typed, instantiateRenderables(typed))
  }
  protected def instantiateRenderables(source: A): Seq[Renderable]
}

final class NullVisualization[A <: Visualizable[_]] extends Visualization[A] {
  override protected def createDerived() = new NullVisualization[A]
  override protected def instantiateRenderables(source: A) = Nil
}

object VisualizationProperty {
  case class ValueChanged[V, C <: VisualizationProperty[V,C]](source: VisualizationProperty[V,C]) extends Event
}

trait VisualizationProperty[V, C <: VisualizationProperty[V,C]] extends Derivable[C] with EdtPublisher {
  private var _value: Option[V] = None
  final def value: V = {
    _value.getOrElse(defaultValue)
  }
  final def value_=(newValue: V): Unit = this.synchronized {
    if (newValue != value) {
      _value = Some(newValue)
      derived.foreach(_.value = newValue)
      publish(VisualizationProperty.ValueChanged(this))
    }
  }

  def defaultValue: V

  final override protected def createDerived(): C = {
    val child = newInstance()
    child.value = this.value
    child
  }

  def newInstance(): C
}