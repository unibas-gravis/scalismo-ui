package scalismo.ui

import java.awt.Color
import java.io.File

import breeze.linalg.DenseVector
import scalismo.geometry.{ Landmark => CLandmark, Point, Vector, _3D }
import scalismo.io.LandmarkIO
import scalismo.ui.util.EdtUtil
import scalismo.ui.visualization._
import scalismo.ui.visualization.props.{ ColorProperty, OpacityProperty, RadiusesProperty, RotationProperty }

import scala.collection.immutable
import scala.swing.event.Event
import scala.util.Try

trait Landmark extends Nameable with Removeable with HasUncertainty[_3D] {
  def point: Point[_3D]
}

class ReferenceLandmark(initalpoint: Point[_3D]) extends Landmark with DirectlyRepositionable with HasUncertainty.SimpleImplementation[_3D] {
  private var _point = initalpoint

  override def point = _point

  override def getCurrentPosition = _point

  override def setCurrentPosition(newPosition: Point[_3D]) = {
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

  private[ui] case class LandmarksChangedInternal(source: AnyRef) extends Event

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

  def create(peer: Point[_3D], name: Option[String], uncertainty: Uncertainty[_3D]): Unit

  override def add(lm: L): Unit = {
    super.add(lm)
    publishEdt(Landmarks.LandmarksChangedInternal(this))
    publishEdt(Landmarks.LandmarksChanged(this))
  }

  override def remove(lm: L, silent: Boolean) = {
    val changed = super.remove(lm, silent)
    if (changed) {
      publishEdt(Landmarks.LandmarksChangedInternal(this))
      publishEdt(Landmarks.LandmarksChanged(this))
    }
    changed
  }

  override def saveToFile(file: File): Try[Unit] = {
    val seq = children.map { lm =>
      val u = Uncertainty.toNDimensionalNormalDistribution(lm.uncertainty)
      CLandmark(lm.name, lm.point, uncertainty = Some(u))
    }.toList
    LandmarkIO.writeLandmarksJson(file, seq)
  }

  override def loadFromFile(file: File): Try[Unit] = {
    this.removeAll()
    val legacyFormat = file.getName.toLowerCase.endsWith("csv")
    val status = for {
      saved <- if (legacyFormat) LandmarkIO.readLandmarksCsv[_3D](file) else LandmarkIO.readLandmarksJson[_3D](file)
      unused = saved.map {
        case CLandmark(name, point, _, uncertainty) =>
          val u = uncertainty.map(nd => Uncertainty.fromNDimensionalNormalDistribution(nd))
          this.create(point, Some(name), u.getOrElse(Uncertainty.defaultUncertainty3D()))
      }
    } yield {}
    publishEdt(Landmarks.LandmarksChangedInternal(this))
    publishEdt(Landmarks.LandmarksChanged(this))
    status
  }
}

object VisualizableLandmark extends SimpleVisualizationFactory[VisualizableLandmark] {
  visualizations += Tuple2(Viewport.ThreeDViewportClassName, Seq(new ThreeDVisualizationAsEllipsoid(None)))
  visualizations += Tuple2(Viewport.TwoDViewportClassName, Seq(new NullVisualization[VisualizableLandmark]))

  class ThreeDVisualizationAsEllipsoid(from: Option[ThreeDVisualizationAsEllipsoid]) extends Visualization[VisualizableLandmark] with EllipsoidLike {
    override val color: ColorProperty = if (from.isDefined) from.get.color.derive() else new ColorProperty(Some(Color.BLUE))
    override val opacity: OpacityProperty = if (from.isDefined) from.get.opacity.derive() else new OpacityProperty(Some(1.0))
    // FIXME: smarter determination of radiuses and rotation
    override val radiuses: RadiusesProperty[_3D] = if (from.isDefined) from.get.radiuses.derive() else new RadiusesProperty(Some(Vector(3.0f, 3.0f, 3.0f)))
    override val rotation: RotationProperty = if (from.isDefined) from.get.rotation.derive() else new RotationProperty(None)

    override protected def createDerived() = new ThreeDVisualizationAsEllipsoid(Some(this))

    override protected def instantiateRenderables(source: VisualizableLandmark) = immutable.Seq(new EllipsoidRenderable(source, color, opacity, radiuses, rotation))

    override val description = "Ellipsoid"
  }

  class EllipsoidRenderable(source: VisualizableLandmark, override val color: ColorProperty, override val opacity: OpacityProperty, override val radiuses: RadiusesProperty[_3D], override val rotation: RotationProperty) extends Renderable with EllipsoidLike {
    radiuses.value = source.uncertainty.stdDevs
    rotation.value = Some(source.uncertainty.rotationMatrix)
    setCenter(source)
    listenTo(source)
    reactions += {
      case Landmarks.LandmarkChanged(src) => setCenter(src)
      case HasUncertainty.UncertaintyChanged(_) =>
        radiuses.value = source.uncertainty.stdDevs
        rotation.value = Some(source.uncertainty.rotationMatrix)
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

  def addAt(position: Point[_3D], nameOption: Option[String], uncertainty: Uncertainty[_3D])

  protected[ui] override def visualizationProvider = VisualizableLandmark
}

class ReferenceLandmarks(val shapeModel: ShapeModel) extends Landmarks[ReferenceLandmark] {
  lazy val nameGenerator: NameGenerator = NameGenerator.defaultGenerator

  def create(template: ReferenceLandmark): Unit = {
    create(template.point, Some(template.name), template.uncertainty)
  }

  def create(peer: Point[_3D], name: Option[String], uncertainty: Uncertainty[_3D]): Unit = {
    val lm = new ReferenceLandmark(peer)
    lm.name = name.getOrElse(nameGenerator.nextName)
    lm.uncertainty = uncertainty
    add(lm)
  }
}

class StaticLandmark(initialCenter: Point[_3D], container: StaticLandmarks) extends VisualizableLandmark(container) with DirectlyRepositionable with HasUncertainty.SimpleImplementation[_3D] {
  var _point = initialCenter

  override def point = _point

  override def getCurrentPosition = _point

  override def setCurrentPosition(newPosition: Point[_3D]) {
    if (_point != newPosition) {
      _point = newPosition
      publishEdt(Landmarks.LandmarkChanged(this))
      publishEdt(Repositionable.CurrentPositionChanged(this))
    }
  }

}

class StaticLandmarks(theObject: ThreeDObject) extends VisualizableLandmarks(theObject) {
  lazy val nameGenerator: NameGenerator = NameGenerator.defaultGenerator

  override def addAt(peer: Point[_3D], name: Option[String], uncertainty: Uncertainty[_3D]) = create(peer, name, uncertainty)

  override def create(peer: Point[_3D], name: Option[String], uncertainty: Uncertainty[_3D]): Unit = {
    val lm = new StaticLandmark(peer, this)
    lm.name = name.getOrElse(nameGenerator.nextName)
    lm.uncertainty = uncertainty
    add(lm)
  }
}

class MoveableLandmark(container: MoveableLandmarks, source: ReferenceLandmark) extends VisualizableLandmark(container) with IndirectlyRepositionable {
  override def name = source.name

  override def name_=(newName: String): Unit = source.name = newName

  override def directlyRepositionableObject = source

  listenTo(container.instance.meshRepresentation, source)

  override def remove() = {
    // we simply forward the request to the source, which in turn publishes an event that all attached
    // moveable landmarks get. And only then we invoke the actual remove functionality (in the reactions below)
    source.remove()
  }

  override def uncertainty: Uncertainty[_3D] = source.uncertainty

  override def uncertainty_=(newValue: Uncertainty[_3D]): Unit = source.uncertainty = newValue

  reactions += {
    case Mesh.GeometryChanged(m) => setCenter()
    case Nameable.NameChanged(n) if n eq source => publishEdt(Nameable.NameChanged(this))

    case Repositionable.CurrentPositionChanged(s) if s eq source => setCenter()
    case HasUncertainty.UncertaintyChanged(s) if s eq source => publishEdt(HasUncertainty.UncertaintyChanged(this))
    case Removeable.Removed(r) if r eq source =>
      deafTo(container.instance.meshRepresentation, source)
      container.remove(this, silent = true)
      publishEdt(Removeable.Removed(this))
  }

  private var _point = calculateCenter()

  def calculateCenter(): Point[_3D] = {
    val (_, ptId) = container.instance.shapeModel.peer.referenceMesh.findClosestPoint(source.point)
    val coeffs = DenseVector(container.instance.coefficients.toArray)
    val mesh = container.instance.shapeModel.peer.instance(coeffs)
    mesh.point(ptId)
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

  override def addAt(peer: Point[_3D], name: Option[String], uncertainty: Uncertainty[_3D]) = {
    create(peer, name, uncertainty)
  }

  override def create(peer: Point[_3D], name: Option[String], uncertainty: Uncertainty[_3D]): Unit = {
    val index = instance.meshRepresentation.peer.findClosestPoint(peer)._2
    val refPoint = instance.shapeModel.peer.referenceMesh.point(index)
    instance.shapeModel.landmarks.create(refPoint, name, uncertainty)
  }

  listenTo(peer)

  reactions += {
    case Landmarks.LandmarksChangedInternal(source) if source eq peer => syncWithPeer()
  }

  syncWithPeer()

  def syncWithPeer() {
    children.length until peer.children.length foreach {
      i =>
        add(new MoveableLandmark(this, peer(i)))
    }
  }
}