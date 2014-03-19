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
import org.statismo.stk.core.utils.VTKHelpers

class Visualizations {
  private type ViewportOrClassName = Either[Viewport, String]

  private val perviewport = new WeakHashMap[ViewportOrClassName, PerViewport]

  private class PerViewport(val context: ViewportOrClassName) {
    private val mappings = new WeakHashMap[VisualizationProvider[_], Try[VisualizationOld[_]]]

    def apply[A <: Visualizable[A]] (key: VisualizationProvider[A]) : Try[VisualizationOld[A]] = {
      val value = mappings.getOrElseUpdate(key, {
        val existing: Try[VisualizationOld[A]] = key match {
          case fac: VisualizationFactory[A] => {
            context match {
              case Left(viewport) => Visualizations.this.apply(key, viewport.getClass.getCanonicalName)
              case Right(vpClass) => Try{fac.instantiate(vpClass)}
            }
          }
          case _ => apply(key.parentVisualizationProvider)
        }
        existing match {
          case Success(ok) => Try{ok.derive()}
          case f@Failure(e) => f
        }
      })
      value.asInstanceOf[Try[VisualizationOld[A]]]
    }
  }

  private def apply[A <: Visualizable[A]] (key: VisualizationProvider[A], context: ViewportOrClassName): Try[VisualizationOld[A]] = {
    val delegate = perviewport.getOrElseUpdate(context, new PerViewport(context))
    delegate(key)
  }

  def apply[A <: Visualizable[A]] (key: VisualizationProvider[A], context: Viewport): Try[VisualizationOld[A]] = apply(key, Left(context))
  def apply[A <: Visualizable[A]] (key: VisualizationProvider[A], context: String): Try[VisualizationOld[A]] = apply(key, Right(context))
}

trait VisualizationFactory[A <: Visualizable[_]] extends VisualizationProvider[A] {
  override final val parentVisualizationProvider = null

  def visualizationsFor(viewportClassName: String): Seq[VisualizationOld[A]]
  final def instantiate(viewportClassName: String): VisualizationOld[A] = {
    visualizationsFor(viewportClassName).headOption match {
      case Some(v) => v
      case None => throw new RuntimeException(getClass+ " did not provide any Visualization options for viewport class "+viewportClassName)
    }
  }
}

trait SimpleVisualizationFactory[A <: Visualizable[_]] extends VisualizationFactory[A] {
  protected var visualizations = new HashMap[String, Seq[VisualizationOld[A]]]
  final override def visualizationsFor(viewportClassName: String): Seq[VisualizationOld[A]] = visualizations.getOrElse(viewportClassName, Nil)
}

trait VisualizationProvider[A <: Visualizable[_]] {
  def parentVisualizationProvider: VisualizationProvider[A]
}

trait Visualizable[X <: Visualizable[X]] extends VisualizationProvider[X] {
}

trait Renderable

abstract class VisualizationOld[A <: Visualizable[_]](template: Option[VisualizationOld[A]]) extends DerivableOld[VisualizationOld[A]](template) {
  private val mappings = new WeakHashMap[A, Seq[Renderable]]
  final def apply(target: A) = mappings.getOrElseUpdate(target, renderablesFor(target))
  def renderablesFor(target: A): Seq[Renderable]
  //protected val properties: Seq[VisualizationProperty[_]]
}

abstract class DerivableOld[A <: DerivableOld[A]](template: Option[A]) {
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

trait Derivable[A <: AnyRef] {
  protected val self:A = this.asInstanceOf[A]
  protected [visualization] var derived: Seq[WeakReference[A]] = Nil

  final def derive(): A = this.synchronized {
    val child: A = createDerived()
    purgeDerived()
    derived :+= new WeakReference[A](child)
    child
  }

  protected [visualization] def purgeDerived(): Unit = this.synchronized {
    val purged = derived.filter(w => w.get != None)
    if (purged.length != derived.length) {
      //FIXME
      println("purged children: " + derived.length + " -> "+purged.length)
      derived = purged
    }
  }

  protected def createDerived() :A
}

trait Visualization[A <: Visualizable[_], C <: Visualization[A,C]] extends Derivable[C] {
  private val mappings = new WeakHashMap[A, Seq[Renderable]]
  final def apply(target: A) = mappings.getOrElseUpdate(target, renderablesFor(target))
  def renderablesFor(target: A): Seq[Renderable]
  //protected val properties: Seq[VisualizationProperty[_]]
}

object VisualizationProperty {
  case class ValueChanged[V, C <: VisualizationProperty[V,C]](source: VisualizationProperty[V,C]) extends Event
}

trait VisualizationProperty[V, C <: VisualizationProperty[V,C]] extends Derivable[C] with EdtPublisher {
  def defaultValue: V
  final def value: V = _value.getOrElse(defaultValue)
  final def value_=(newValue: V): Unit = this.synchronized {
    if (newValue != value) {
      _value = Some(newValue)
      publish(VisualizationProperty.ValueChanged(this))
      var purge = false
      derived.foreach { w =>
        w.get match {
          case None => purge = true
          case Some(c) => c.value = newValue
        }
      }
      if (purge) {
        purgeDerived()
      }
    }
  }
  final override protected def createDerived(): C = {
    val child = this.getClass.newInstance().asInstanceOf[C]
    child.value = this.value
    child
  }
  private var _value: Option[V] = None
}

class ColorProperty extends VisualizationProperty[Color, ColorProperty] {
  override lazy val defaultValue = Color.WHITE
}

class LMVisualizationOld(template: Option[LMVisualizationOld]) extends VisualizationOld[LM](template) {
  val color: ColorProperty = if (template.isDefined) template.get.color.derive() else new ColorProperty
  //override val properties = Seq(color)
  def renderablesFor(target: LM) = Nil
  //  def instantiateNew(template: Option[LMVisualization]) = new LMVisualization(template)
  override protected def instantiateNew(template: Option[VisualizationOld[LM]]): LMVisualizationOld = new LMVisualizationOld(template.asInstanceOf[Option[LMVisualizationOld]])
}

class LMVisualization(template: Option[LMVisualization]) extends Visualization[LM, LMVisualization] {
  val color: ColorProperty = if (template.isDefined) template.get.color.derive() else new ColorProperty
  //override val properties = Seq(color)
  def renderablesFor(target: LM) = Nil
  //  def instantiateNew(template: Option[LMVisualization]) = new LMVisualization(template)
//  override protected def instantiateNew(template: Option[Visualization[LM]]): LMVisualization = new LMVisualization(template.asInstanceOf[Option[LMVisualization]])
  override protected def createDerived(): LMVisualization = {
    ???
  }
}

object X extends Viewport {
  override def isMouseSensitive: Boolean = ???

  override def scene: Scene = ???
}

object LMS extends SimpleVisualizationFactory[LM] {
  visualizations += Tuple2("org.statismo.stk.ui.visualization.X$", Seq(new LMVisualizationOld(None)))
}

class LMS extends VisualizationProvider[LM] {
  override val parentVisualizationProvider = LMS
}

class LM extends Visualizable[LM] {
  val lms: LMS = new LMS()
  override val parentVisualizationProvider = lms
}

object VisTest extends App {
  val p = X

  val v = new Visualizations
  val lm = new LM
  val v3 = v(lm.lms, p).get.asInstanceOf[LMVisualizationOld]
  v3.color.value = Color.BLUE
  val v4 = v(lm, p).get.asInstanceOf[LMVisualizationOld]
  //v(lm, p.getClass).get.asInstanceOf[LMVisualization]
  //v(lm.lms, p.getClass).get.asInstanceOf[LMVisualization]

  println(v4.color.value)
  println(v3.color.value)
  v4.color.value = Color.RED
  println(v4.color.value)
  println(v3.color.value)
  v3.color.value = Color.GREEN
  println(v4.color.value)
  println(v3.color.value)
  v4.color.value = Color.BLACK
  println(v4.color.value)
  println(v3.color.value)
}