package org.statismo.stk.ui

import java.awt.Color
import java.io.File

import scala.swing.event.Event
import scala.util.Try

import org.statismo.stk.core.geometry.Point3D
import org.statismo.stk.core.geometry.ThreeD
import org.statismo.stk.core.io.LandmarkIO

import breeze.linalg.DenseVector

trait Landmark extends Nameable with Removeable {
  def peer: Point3D
}

class ReferenceLandmark(val peer: Point3D) extends Landmark

class DisplayableLandmark(container: DisplayableLandmarks) extends Landmark with Displayable with SphereLike {
  override lazy val parent = container
  color = container.color
  opacity = container.opacity
  radius = container.radius
  def peer = center
}

class StaticLandmark(initialCenter: Point3D, container: StaticLandmarks) extends DisplayableLandmark(container) {
  center = initialCenter
}

class MoveableLandmark(container: MoveableLandmarks, source: ReferenceLandmark) extends DisplayableLandmark(container) {
  override lazy val parent = container

  name = source.name
  listenTo(container.instance.meshRepresentation, source)

  override def remove = {
    // we simply forward the request to the source, which in turn publishes an event that all attached
    // moveable landmarks get. And only then we invoke the actual remove functionality (in the reactions below)
    source.remove
  }

  reactions += {
    case Mesh.GeometryChanged(m) => setCenter
    case Nameable.NameChanged(n) =>
      if (n == source) {
        this.name = source.name
      } else if (n == this) {
        source.name = this.name
      }
    case Removeable.Removed(r) =>
      if (r eq source) {
        parent.remove(this, silent = true)
      }
  }

  setCenter

  def setCenter = {
    val coeffs = DenseVector(container.instance.coefficients.toArray)
    center = source.peer + container.instance.shapeModel.gaussianProcess.instance(coeffs)(source.peer)
  }

}

object Landmarks extends FileIoMetadata {
  case class LandmarksChanged(source: AnyRef) extends Event
  override val description = "Landmarks"
  override val fileExtensions = Seq("csv")
}

trait Landmarks[L <: Landmark] extends MutableObjectContainer[L] with EdtPublisher with Saveable with Loadable {
  val saveableMetadata = Landmarks
  val loadableMetadata = Landmarks

  override def isCurrentlySaveable: Boolean = !children.isEmpty

  def create(peer: Point3D, name: Option[String]): Unit

  override def add(lm: L): Unit = {
    super.add(lm)
    publish(Landmarks.LandmarksChanged(this))
  }

  override def remove(lm: L, silent: Boolean) = {
    val changed = super.remove(lm, silent)
    if (changed) publish(Landmarks.LandmarksChanged(this))
    changed
  }

  override def saveToFile(file: File): Try[Unit] = {
    val seq = children.map { lm => (lm.name, lm.peer) }.toIndexedSeq
    LandmarkIO.writeLandmarks[ThreeD](file, seq)
  }

  override def loadFromFile(file: File): Try[Unit] = {
    this.removeAll
    val status = for {
      saved <- LandmarkIO.readLandmarks3D(file)
      val newLandmarks = {
        saved.map {
          case (name, point) =>
            this.create(point, Some(name))
        }
      }
    } yield {}
    publish(Landmarks.LandmarksChanged(this))
    status
  }
}

abstract class DisplayableLandmarks(theObject: ThreeDObject) extends StandaloneSceneTreeObjectContainer[DisplayableLandmark] with Landmarks[DisplayableLandmark] with Radius with Colorable with RemoveableChildren {
  name = "Landmarks"
  override lazy val isNameUserModifiable = false
  override lazy val parent = theObject
  def addAt(position: Point3D)

  color = Color.BLUE
  opacity = 0.8
  radius = 3.0f

  override def opacity_=(newOpacity: Double) {
    super.opacity_=(newOpacity)
    children.foreach { c =>
      c.opacity = newOpacity
    }
  }

  override def color_=(newColor: Color) {
    super.color_=(newColor)
    children.foreach { c =>
      c.color = newColor
    }
  }

  override def radius_=(newRadius: Float) {
    super.radius_=(newRadius)
    children.foreach { c =>
      c.radius = newRadius
    }
  }

}

class ReferenceLandmarks(val shapeModel: ShapeModel) extends Landmarks[ReferenceLandmark] {
  lazy val nameGenerator: NameGenerator = NameGenerator.defaultGenerator

  def create(template: ReferenceLandmark): Unit = {
    create(template.peer, Some(template.name))
  }

  def create(peer: Point3D, name: Option[String] = None): Unit = {
    val lm = new ReferenceLandmark(peer)
    lm.name = name.getOrElse(nameGenerator.nextName)
    add(lm)
  }
}

class StaticLandmarks(theObject: ThreeDObject) extends DisplayableLandmarks(theObject) {
  lazy val nameGenerator: NameGenerator = NameGenerator.defaultGenerator

  def addAt(peer: Point3D) = create(peer)

  def create(peer: Point3D, name: Option[String] = None): Unit = {
    val lm = new StaticLandmark(peer, this)
    lm.name = name.getOrElse(nameGenerator.nextName)
    add(lm)
  }

}

class MoveableLandmarks(val instance: ShapeModelInstance) extends DisplayableLandmarks(instance) {
  val peer = instance.shapeModel.landmarks

  def addAt(peer: Point3D) = {
    create(peer, None)
  }

  def create(peer: Point3D, name: Option[String]): Unit = {
    val index = instance.meshRepresentation.peer.findClosestPoint(peer)._2
    val refPoint = instance.shapeModel.peer.mesh.points(index).asInstanceOf[Point3D]
    instance.shapeModel.landmarks.create(refPoint, name)
  }

  listenTo(peer)

  reactions += {
    case Landmarks.LandmarksChanged(source) =>
      if (source eq peer) {
        syncWithPeer
      }
  }

  syncWithPeer

  def syncWithPeer = {
    var changed = false
    _children.length until peer.children.length foreach { i =>
      changed = true
      val p = peer(i)
      add(new MoveableLandmark(this, peer(i)))
    }
    if (changed) {
      publish(SceneTreeObject.ChildrenChanged(this))
    }
  }

  override def remove {
    deafTo(peer)
    super.remove
  }
}