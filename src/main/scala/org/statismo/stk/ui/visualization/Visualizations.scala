package org.statismo.stk.ui.visualization

import org.statismo.stk.ui.{visualization, EdtPublisher, Viewport, Scene}
import scala.util.{Failure, Success, Try}
import java.awt.Color
import scala.ref.WeakReference
import scala.swing.event.Event
import scala.language.existentials
import scala.collection.immutable.Seq
import scala.collection.immutable.HashMap
import scala.collection.mutable.WeakHashMap

class Visualizations {
  private type ViewportOrClass = Either[Viewport, Class[_ <: Viewport]]

  private val perviewport = new WeakHashMap[ViewportOrClass, PerViewport]

  private class PerViewport(val context: ViewportOrClass) {
    private val mappings = new WeakHashMap[VisualizationProvider[_], Try[Visualization[_]]]

    def apply[A <: Visualizable[A]] (key: VisualizationProvider[A]) : Try[Visualization[A]] = {
      val value = mappings.getOrElseUpdate(key, {
        val existing: Try[Visualization[A]] = key match {
          case fac: VisualizationFactory[A] => {
            context match {
              case Left(viewport) => Visualizations.this.apply(key, viewport.getClass)
              case Right(vpClass) => Try{fac.instantiate(vpClass.getCanonicalName)}
            }
          }
          case _ => apply(key.parentVisualizationProvider)
        }
        existing match {
          case Success(ok) => Try{ok.derive()}
          case f@Failure(e) => f
        }
      })
      value.asInstanceOf[Try[Visualization[A]]]
    }
  }

  private def apply[A <: Visualizable[A]] (key: VisualizationProvider[A], context: ViewportOrClass): Try[Visualization[A]] = {
    val delegate = perviewport.getOrElseUpdate(context, new PerViewport(context))
    delegate(key)
  }

  def apply[A <: Visualizable[A]] (key: VisualizationProvider[A], context: Viewport): Try[Visualization[A]] = apply(key, Left(context))
  def apply[A <: Visualizable[A]] (key: VisualizationProvider[A], context: Class[_ <: Viewport]): Try[Visualization[A]] = apply(key, Right(context))
}

trait VisualizationFactory[A <: Visualizable[_]] extends VisualizationProvider[A] {
  override final val parentVisualizationProvider = null

  def visualizationsFor(viewportClassName: String): Seq[Visualization[A]]
  final def instantiate(viewportClassName: String): Visualization[A] = {
    visualizationsFor(viewportClassName).headOption match {
      case Some(v) => v
      case None => throw new RuntimeException(getClass+ " did not provide any Visualization options for viewport class "+viewportClassName)
    }
  }
}

trait SimpleVisualizationFactory[A <: Visualizable[_]] extends VisualizationFactory[A] {
  protected var visualizations = new HashMap[String, Seq[Visualization[A]]]
  final override def visualizationsFor(viewportClassName: String): Seq[Visualization[A]] = visualizations.getOrElse(viewportClassName, Nil)
}

trait VisualizationProvider[A <: Visualizable[_]] {
  def parentVisualizationProvider: VisualizationProvider[A]
}

trait Visualizable[X <: Visualizable[X]] extends VisualizationProvider[X] {
}

trait Renderable

abstract class Visualization[A <: Visualizable[_]](template: Option[Visualization[A]]) extends Derivable[Visualization[A]](template) {
  private val mappings = new WeakHashMap[A, Seq[Renderable]]
  final def apply(target: A) = mappings.getOrElseUpdate(target, renderablesFor(target))
  def renderablesFor(target: A): Seq[Renderable]
  protected val properties: Seq[VisualizationProperty[_]]
}

abstract class Derivable[A <: Derivable[A]](template: Option[A]) {
  protected [visualization] var children: Seq[WeakReference[A]] = Nil
  private [visualization] final def derive(): A = this.synchronized {
    val child = instantiateNew(Some(this.asInstanceOf[A]))
    purgeChildren()
    children :+= new WeakReference[A](child)
    child
  }

  protected [visualization] def purgeChildren(): Unit = this.synchronized {
    val purged = children.filter(w => w.get != None)
    if (purged.length != children.length) {
      //FIXME
      println("purged children: " + children.length + " -> "+purged.length)
      children = purged
    }
  }

  protected def instantiateNew(template: Option[A]):A
}

object VisualizationProperty {
  case class ValueChanged(source: VisualizationProperty[_]) extends Event
}

abstract class VisualizationProperty[A](template: Option[VisualizationProperty[A]]) extends Derivable[VisualizationProperty[A]](template) with EdtPublisher {
  def defaultValue: A
  private var _value: A = template match {
    case Some(t) => t.value
    case None => defaultValue
  }
  final def value: A = _value
  final def value_=(newValue: A): Unit = this.synchronized {
    if (newValue != _value) {
      _value = newValue
      publish(VisualizationProperty.ValueChanged(this))
      var purge = false
      children.foreach { w =>
        w.get match {
          case None => purge = true
          case Some(c) => c.value = newValue
        }
      }
      if (purge) {
        purgeChildren()
      }
    }
  }
}

class ColorProperty(template: Option[VisualizationProperty[Color]]) extends VisualizationProperty[Color](template) {
  override lazy val defaultValue = Color.WHITE

  override protected def instantiateNew(template: Option[VisualizationProperty[Color]]): VisualizationProperty[Color] = new ColorProperty(template)
}

class LMVisualization(template: Option[LMVisualization]) extends Visualization[LM](template) {
  val color: ColorProperty = if (template.isDefined) template.get.color.derive().asInstanceOf[ColorProperty] else new ColorProperty(None)
  override val properties = Seq(color)
  def renderablesFor(target: LM) = Nil
//  def instantiateNew(template: Option[LMVisualization]) = new LMVisualization(template)
  override protected def instantiateNew(template: Option[Visualization[LM]]): LMVisualization = new LMVisualization(template.asInstanceOf[Option[LMVisualization]])
}

object X extends Viewport {
  override def isMouseSensitive: Boolean = ???

  override def scene: Scene = ???
}

object LMS extends SimpleVisualizationFactory[LM] {
  visualizations += Tuple2("org.statismo.stk.ui.visualization.X$", Seq(new LMVisualization(None)))
}

class LMS extends VisualizationProvider[LM] {
  override val parentVisualizationProvider = LMS
}

class LM extends Visualizable[LM] {
  val lms: LMS = new LMS()
  override val parentVisualizationProvider = lms
}

object VisTest extends App {
  val x = new Viewport {override def scene: Scene = ???
    override def isMouseSensitive: Boolean = ???
  }

  val p = X

  val v = new Visualizations
  val lm = new LM
  val v4 = v(lm, p).get.asInstanceOf[LMVisualization]
  val v3 = v(lm.lms, p).get.asInstanceOf[LMVisualization]
  v(lm, p.getClass).get.asInstanceOf[LMVisualization]
  v(lm.lms, p.getClass).get.asInstanceOf[LMVisualization]

  println(v4.color.value)
  println(v3.color.value)
  v4.color.value = Color.RED
  println(v4.color.value)
  println(v3.color.value)
  v3.color.value = Color.GREEN
  println(v4.color.value)
  println(v3.color.value)
}