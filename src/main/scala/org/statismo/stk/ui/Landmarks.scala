package org.statismo.stk.ui

import java.io.File

import scala.swing.event.Event
import scala.util.Try

import org.statismo.stk.core.geometry.ThreeD
import org.statismo.stk.core.io.LandmarkIO
import scala.collection.immutable

import breeze.linalg.DenseVector
import org.statismo.stk.ui.visualization._
import org.statismo.stk.core.geometry.Point3D
import scala.Some
import scala.Tuple2
import org.statismo.stk.ui.visualization.props.{RadiusProperty, ColorProperty, OpacityProperty}
import java.awt.Color

trait Landmark extends Nameable with Removeable {
  def point: Point3D
}

class ReferenceLandmark(val point: Point3D) extends Landmark

object Landmarks extends FileIoMetadata {

  case class LandmarksChanged(source: AnyRef) extends Event

  case class LandmarkChanged(source: Landmark) extends Event

  override val description = "Landmarks"
  override val fileExtensions = immutable.Seq("csv")
}

trait Landmarks[L <: Landmark] extends MutableObjectContainer[L] with EdtPublisher with Saveable with Loadable {
  val saveableMetadata = Landmarks
  val loadableMetadata = Landmarks

  override def isCurrentlySaveable: Boolean = !children.isEmpty

  def create(peer: Point3D, name: Option[String]): Unit

  override def add(lm: L): Unit = this.synchronized {
    super.add(lm)
    publish(Landmarks.LandmarksChanged(this))
  }

  override def remove(lm: L, silent: Boolean) = this.synchronized {
    val changed = super.remove(lm, silent)
    if (changed) publish(Landmarks.LandmarksChanged(this))
    changed
  }

  override def saveToFile(file: File): Try[Unit] = this.synchronized {
    val seq = children.map {
      lm => (lm.name, lm.point)
    }.toIndexedSeq
    LandmarkIO.writeLandmarks[ThreeD](file, seq)
  }

  override def loadFromFile(file: File): Try[Unit] = this.synchronized {
    this.removeAll()
    val status = for {
      saved <- LandmarkIO.readLandmarks3D(file)
      newLandmarks = saved.map {
        case (name, point) =>
          this.create(point, Some(name))
      }
    } yield {}
    publish(Landmarks.LandmarksChanged(this))
    status
  }
}

object VisualizableLandmark extends SimpleVisualizationFactory[VisualizableLandmark] {
  visualizations += Tuple2(Viewport.ThreeDViewportClassName, Seq(new ThreeDVisualizationAsSphere(None)))
  //visualizations += Tuple2(Viewport.ThreeDViewportClassName, Seq(new NullVisualization[VisualizableLandmark]))
  visualizations += Tuple2(Viewport.TwoDViewportClassName, Seq(new NullVisualization[VisualizableLandmark]))

  class ThreeDVisualizationAsSphere(from: Option[ThreeDVisualizationAsSphere]) extends Visualization[VisualizableLandmark] with SphereLike {
    override val color: ColorProperty = if (from.isDefined) from.get.color.derive() else new ColorProperty(Some(Color.BLUE))
    override val opacity: OpacityProperty = if (from.isDefined) from.get.opacity.derive() else new OpacityProperty(Some(1.0))
    override val radius: RadiusProperty = if (from.isDefined) from.get.radius.derive() else new RadiusProperty(Some(3.0f))


    override protected def createDerived() = new ThreeDVisualizationAsSphere(Some(this))

    override protected def instantiateRenderables(source: VisualizableLandmark) = immutable.Seq(new SphereRenderable(source, color, opacity, radius))
  }

  class SphereRenderable(source: VisualizableLandmark, override val color: ColorProperty, override val opacity: OpacityProperty, override val radius: RadiusProperty) extends Renderable with SphereLike {
    setCenter(source)
    listenTo(source)
    reactions += {
      case Landmarks.LandmarkChanged(src) => setCenter(src)
      case SceneTreeObject.Destroyed(src) => deafTo(src)
    }

    def setCenter(src: Landmark): Unit = {
      center = src.point
    }
  }

}

abstract class VisualizableLandmark(container: VisualizableLandmarks) extends Landmark with VisualizableSceneTreeObject[VisualizableLandmark] {
  override def parent = container

  override def visualizationProvider = container
}

abstract class VisualizableLandmarks(theObject: ThreeDObject) extends StandaloneSceneTreeObjectContainer[VisualizableLandmark] with Landmarks[VisualizableLandmark] with VisualizationProvider[VisualizableLandmark] with RemoveableChildren {
  name = "Landmarks"
  override lazy val isNameUserModifiable = false
  override lazy val parent = theObject

  def addAt(position: Point3D)

  override def visualizationProvider = VisualizableLandmark
}

class ReferenceLandmarks(val shapeModel: ShapeModel) extends Landmarks[ReferenceLandmark] {
  lazy val nameGenerator: NameGenerator = NameGenerator.defaultGenerator

  def create(template: ReferenceLandmark): Unit = {
    create(template.point, Some(template.name))
  }

  def create(peer: Point3D, name: Option[String] = None): Unit = this.synchronized {
    val lm = new ReferenceLandmark(peer)
    lm.name = name.getOrElse(nameGenerator.nextName)
    add(lm)
  }
}

class StaticLandmark(initialCenter: Point3D, container: StaticLandmarks) extends VisualizableLandmark(container) {
  var point = initialCenter
}

class StaticLandmarks(theObject: ThreeDObject) extends VisualizableLandmarks(theObject) {
  lazy val nameGenerator: NameGenerator = NameGenerator.defaultGenerator

  def addAt(peer: Point3D) = create(peer)

  def create(peer: Point3D, name: Option[String] = None): Unit = {
    val lm = new StaticLandmark(peer, this)
    lm.name = name.getOrElse(nameGenerator.nextName)
    add(lm)
  }

}

class MoveableLandmark(container: MoveableLandmarks, source: ReferenceLandmark) extends VisualizableLandmark(container) {
  name = source.name
  listenTo(container.instance.meshRepresentation, source)

  override def remove() = {
    // we simply forward the request to the source, which in turn publishes an event that all attached
    // moveable landmarks get. And only then we invoke the actual remove functionality (in the reactions below)
    source.remove()
  }

  reactions += {
    case Mesh.GeometryChanged(m) => setCenter()
    case Nameable.NameChanged(n) =>
      if (n eq source) {
        this.name = source.name
      } else if (n eq this) {
        source.name = this.name
      }
    case Removeable.Removed(r) if r eq source =>
      deafTo(container.instance.meshRepresentation, source)
      container.remove(this, silent = true)
      publish(Removeable.Removed(this))
  }

  var point = calculateCenter()

  def calculateCenter(): Point3D = {
    val coeffs = DenseVector(container.instance.coefficients.toArray)
    source.point + container.instance.shapeModel.gaussianProcess.instance(coeffs)(source.point)
  }

  def setCenter(): Unit = {
    point = calculateCenter()
    publish(Landmarks.LandmarkChanged(this))
  }

}

class MoveableLandmarks(val instance: ShapeModelInstance) extends VisualizableLandmarks(instance) {
  val peer = instance.shapeModel.landmarks

  def addAt(peer: Point3D) = this.synchronized {
    create(peer, None)
  }

  override def create(peer: Point3D, name: Option[String]): Unit = this.synchronized {
    val index = instance.meshRepresentation.peer.findClosestPoint(peer)._2
    val refPoint = instance.shapeModel.peer.mesh.points(index).asInstanceOf[Point3D]
    instance.shapeModel.landmarks.create(refPoint, name)
  }

  listenTo(peer)

  reactions += {
    case Landmarks.LandmarksChanged(source) =>
      if (source eq peer) {
        syncWithPeer()
      }
  }

  syncWithPeer()

  def syncWithPeer() = {
    var changed = false
    _children.length until peer.children.length foreach {
      i =>
        changed = true
        add(new MoveableLandmark(this, peer(i)))
    }
    if (changed) {
      publish(SceneTreeObject.ChildrenChanged(this))
    }
  }
}