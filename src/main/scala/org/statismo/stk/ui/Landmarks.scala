package org.statismo.stk.ui

import java.awt.Color
import java.io.File

import org.statismo.stk.core.geometry.{Landmark => CLandmark, Point, _3D}
import org.statismo.stk.core.io.LandmarkIO
import org.statismo.stk.ui.util.EdtUtil
import org.statismo.stk.ui.visualization._
import org.statismo.stk.ui.visualization.props.{ColorProperty, OpacityProperty, RadiusProperty}

import scala.collection.immutable
import scala.swing.event.Event
import scala.util.Try

import breeze.linalg.DenseVector

trait Landmark extends Nameable with Removeable {
  def point: Point[_3D]
}

class ReferenceLandmark(initalpoint: Point[_3D]) extends Landmark with DirectlyRepositionable {
  private var _point = initalpoint

  override def point = _point

  override def getCurrentPosition = _point

  override def setCurrentPosition(newPosition: Point[_3D]) = this.synchronized {
    if (_point != newPosition) {
      _point = newPosition
      publishEdt(Landmarks.LandmarkChanged(this))
      publishEdt(Repositionable.CurrentPositionChanged(this))
    }
  }
}

object Landmarks {

  case class LandmarksChanged(source: AnyRef) extends Event

  case class LandmarkChanged(source: Landmark) extends Event

  object ReaderMetadata extends FileIoMetadata {
    override val description = "Landmarks"
    override val fileExtensions = immutable.Seq("csv", "json")
  }

  object WriterMetadata extends FileIoMetadata {
    override val description = "Landmarks"
    override val fileExtensions = immutable.Seq("json")
  }

}

trait Landmarks[L <: Landmark] extends MutableObjectContainer[L] with EdtPublisher with Saveable with Loadable {
  val saveableMetadata = Landmarks.WriterMetadata
  val loadableMetadata = Landmarks.ReaderMetadata

  override def isCurrentlySaveable: Boolean = children.nonEmpty

  def create(peer: Point[_3D], name: Option[String]): Unit

  override def add(lm: L): Unit = this.synchronized {
    super.add(lm)
    publishEdt(Landmarks.LandmarksChanged(this))
  }

  override def remove(lm: L, silent: Boolean) = this.synchronized {
    val changed = super.remove(lm, silent)
    if (changed) publishEdt(Landmarks.LandmarksChanged(this))
    changed
  }

  override def saveToFile(file: File): Try[Unit] = this.synchronized {
    val seq = children.map {
      lm => CLandmark(lm.name, lm.point)
    }.toList
    LandmarkIO.writeLandmarksJson(file, seq)
  }

  override def loadFromFile(file: File): Try[Unit] = this.synchronized {
    this.removeAll()
    val legacyFormat = file.getName.toLowerCase.endsWith("csv")
    val status = for {
      saved <- if (legacyFormat) LandmarkIO.readLandmarksCsv[_3D](file) else LandmarkIO.readLandmarksJson[_3D](file)
      newLandmarks = saved.map {
        case CLandmark(name, point, _, _) =>
          this.create(point, Some(name))
      }
    } yield {}
    publishEdt(Landmarks.LandmarksChanged(this))
    status
  }
}

object VisualizableLandmark extends SimpleVisualizationFactory[VisualizableLandmark] {
  visualizations += Tuple2(Viewport.ThreeDViewportClassName, Seq(new ThreeDVisualizationAsSphere(None)))
  visualizations += Tuple2(Viewport.TwoDViewportClassName, Seq(new NullVisualization[VisualizableLandmark]))

  class ThreeDVisualizationAsSphere(from: Option[ThreeDVisualizationAsSphere]) extends Visualization[VisualizableLandmark] with SphereLike {
    override val color: ColorProperty = if (from.isDefined) from.get.color.derive() else new ColorProperty(Some(Color.BLUE))
    override val opacity: OpacityProperty = if (from.isDefined) from.get.opacity.derive() else new OpacityProperty(Some(1.0))
    override val radius: RadiusProperty = if (from.isDefined) from.get.radius.derive() else new RadiusProperty(Some(3.0f))


    override protected def createDerived() = new ThreeDVisualizationAsSphere(Some(this))

    override protected def instantiateRenderables(source: VisualizableLandmark) = immutable.Seq(new SphereRenderable(source, color, opacity, radius))

    override val description = "Sphere"
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

  protected[ui] override def visualizationProvider = container
}

abstract class VisualizableLandmarks(theObject: ThreeDObject) extends StandaloneSceneTreeObjectContainer[VisualizableLandmark] with Landmarks[VisualizableLandmark] with VisualizationProvider[VisualizableLandmark] with RemoveableChildren {
  name = "Landmarks"
  override lazy val isNameUserModifiable = false
  override lazy val parent = theObject

  def addAt(position: Point[_3D], nameOption: Option[String])

  protected[ui] override def visualizationProvider = VisualizableLandmark
}

class ReferenceLandmarks(val shapeModel: ShapeModel) extends Landmarks[ReferenceLandmark] {
  lazy val nameGenerator: NameGenerator = NameGenerator.defaultGenerator

  def create(template: ReferenceLandmark): Unit = {
    create(template.point, Some(template.name))
  }

  def create(peer: Point[_3D], name: Option[String] = None): Unit = this.synchronized {
    val lm = new ReferenceLandmark(peer)
    lm.name = name.getOrElse(nameGenerator.nextName)
    add(lm)
  }
}

class StaticLandmark(initialCenter: Point[_3D], container: StaticLandmarks) extends VisualizableLandmark(container) with DirectlyRepositionable {
  var _point = initialCenter

  override def point = _point

  override def getCurrentPosition = _point

  override def setCurrentPosition(newPosition: Point[_3D]) = this.synchronized {
    if (_point != newPosition) {
      _point = newPosition
      publishEdt(Landmarks.LandmarkChanged(this))
      publishEdt(Repositionable.CurrentPositionChanged(this))
    }
  }
}

class StaticLandmarks(theObject: ThreeDObject) extends VisualizableLandmarks(theObject) {
  lazy val nameGenerator: NameGenerator = NameGenerator.defaultGenerator

  def addAt(peer: Point[_3D], nameOpt: Option[String] = None) = create(peer, nameOpt)

  def create(peer: Point[_3D], name: Option[String] = None): Unit = {
    val lm = new StaticLandmark(peer, this)
    lm.name = name.getOrElse(nameGenerator.nextName)
    add(lm)
  }
}

class MoveableLandmark(container: MoveableLandmarks, source: ReferenceLandmark) extends VisualizableLandmark(container) with IndirectlyRepositionable {
  override def name = source.name

  override def directlyRepositionableObject = source

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
    case Repositionable.CurrentPositionChanged(r) =>
      if (r eq source) {
        setCenter()
      }
    case Removeable.Removed(r) if r eq source =>
      deafTo(container.instance.meshRepresentation, source)
      container.remove(this, silent = true)
      publishEdt(Removeable.Removed(this))
  }

  private var _point = calculateCenter()

  def calculateCenter(): Point[_3D] = {
    val coeffs = DenseVector(container.instance.coefficients.toArray)
    val (_, ptId) = container.instance.shapeModel.peer.referenceMesh.findClosestPoint(source.point)
    container.instance.shapeModel.peer.instance(coeffs)(ptId)
  }

  def setCenter(): Unit = {
    _point = calculateCenter()
    publishEdt(Landmarks.LandmarkChanged(this))
    publishEdt(Repositionable.CurrentPositionChanged(this))
  }

  override def point = _point

  override def getCurrentPosition = point
}

class MoveableLandmarks(val instance: ShapeModelInstance) extends VisualizableLandmarks(instance) {
  val peer = instance.shapeModel.landmarks

  def addAt(peer: Point[_3D], name: Option[String] = None) = this.synchronized {
    create(peer, name)
  }

  override def create(peer: Point[_3D], name: Option[String]): Unit = this.synchronized {
    val index = instance.meshRepresentation.peer.findClosestPoint(peer)._2
    val refPoint = instance.shapeModel.peer.referenceMesh.points(index)
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

  def syncWithPeer() = EdtUtil.onEdt({
    var changed = false
    children.length until peer.children.length foreach {
      i =>
        changed = true
        add(new MoveableLandmark(this, peer(i)))
    }
    if (changed) {
      publishEdt(SceneTreeObject.ChildrenChanged(this))
    }
  }, wait = true)
}