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
    private val mappings = new WeakHashMap[VisualizationProvider[_], Try[Visualization[_]]]

    def tryGet[A <: Visualizable[A]] (key: VisualizationProvider[A]) : Try[Visualization[A]] = {
      val value = mappings.getOrElseUpdate(key, {
        val existing: Try[Visualization[A]] = key match {
          case fac: VisualizationFactory[A] => {
            context match {
              case Left(viewport) => Visualizations.this.tryGet(key, viewport.getClass.getCanonicalName)
              case Right(vpClass) => Try{fac.instantiate(vpClass)}
            }
          }
          case _ => tryGet(key.parentVisualizationProvider)
        }
        existing match {
          case Success(ok) => Try{ok.derive()}
          case f@Failure(e) => f
        }
      })
      value.asInstanceOf[Try[Visualization[A]]]
    }
  }

  private def tryGet[A <: Visualizable[A]] (key: VisualizationProvider[A], context: ViewportOrClassName): Try[Visualization[A]] = {
    val delegate = perviewport.getOrElseUpdate(context, new PerViewport(context))
    delegate.tryGet(key)
  }

  def tryGet[A <: Visualizable[A]] (key: VisualizationProvider[A], context: Viewport): Try[Visualization[A]] = tryGet(key, Left(context))
  def tryGet[A <: Visualizable[A]] (key: VisualizationProvider[A], context: String): Try[Visualization[A]] = tryGet(key, Right(context))
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

trait Visualization[A <: Visualizable[_]] extends Derivable[Visualization[A]] {
  private val mappings = new WeakHashMap[A, Seq[Renderable]]
  final def apply(target: A) = mappings.getOrElseUpdate(target, renderablesFor(target))
  def renderablesFor(target: A): Seq[Renderable]
  //protected val properties: Seq[VisualizationProperty[_]]
}

trait ConcreteVisualization[A <: Visualizable[_], C <: ConcreteVisualization[A,C]] extends Visualization[A] {
}

object VisualizationProperty {
  case class ValueChanged[V, C <: VisualizationProperty[V,C]](source: VisualizationProperty[V,C]) extends Event
}

trait VisualizationProperty[V, C <: VisualizationProperty[V,C]] extends Derivable[C] with EdtPublisher {
  private var _value: Option[V] = None
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

  def defaultValue: V

  final override protected def createDerived(): C = {
    val child = newInstance()
    child.value = this.value
    child
  }

  def newInstance(): C
}

class ColorProperty extends VisualizationProperty[Color, ColorProperty] {
  override lazy val defaultValue = Color.WHITE
  def newInstance() = new ColorProperty
}

class LMVisualizationOne(template: Option[LMVisualizationOne]) extends ConcreteVisualization[LM, LMVisualizationOne] {
  def this() = this(None)
  val color: ColorProperty = if (template.isDefined) template.get.color.derive() else new ColorProperty
  def renderablesFor(target: LM) = Nil
  override protected def createDerived(): LMVisualizationOne = new LMVisualizationOne(Some(this))
}

class LMVisualizationTwo(template: Option[LMVisualizationTwo]) extends ConcreteVisualization[LM, LMVisualizationTwo] {
  def this() = this(None)
  val color: ColorProperty = if (template.isDefined) template.get.color.derive() else new ColorProperty
  def renderablesFor(target: LM) = Nil
  override protected def createDerived(): LMVisualizationTwo = {
    ???
  }
}

object X extends Viewport {
  override def isMouseSensitive: Boolean = ???

  override def scene: Scene = ???
}

object LMS extends SimpleVisualizationFactory[LM] {
  visualizations += Tuple2("org.statismo.stk.ui.visualization.X$", Seq(new LMVisualizationOne))
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
  val v3 = v.tryGet(lm.lms, p).get.asInstanceOf[LMVisualizationOne]
  v3.color.value = Color.BLUE
  val v4 = v.tryGet(lm, p).get.asInstanceOf[LMVisualizationOne]
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