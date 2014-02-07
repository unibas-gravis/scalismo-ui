package org.statismo.stk.ui

import org.statismo.stk.core.geometry.Point3D
import java.awt.Color
import breeze.linalg.DenseVector
import scala.swing.Publisher
import scala.swing.event.Event
import java.io.File
import scala.util.Try
import org.statismo.stk.core.io.LandmarkIO
import org.statismo.stk.core.geometry.ThreeD

trait Landmark extends Nameable with Removeable {
  def peer: Point3D
}

class ReferenceLandmark(val peer: Point3D) extends Landmark

class DisplayableLandmark(container: DisplayableLandmarks) extends Landmark with Displayable with SphereLike{
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
    case Nameable.NameChanged(n) => {
      if (n == source) {
        this.name = source.name
      } else if (n == this) {
        source.name = this.name
      }
    }
    case Removeable.Removed(r) => {
      if (r eq source) {
        super.remove()
      }
    }
  }

  setCenter

  def setCenter = {
    val mesh = container.instance.meshRepresentation.triangleMesh
    val coeffs = DenseVector(container.instance.coefficients.toArray)
    val mappedPt = source.peer + container.instance.model.gp.instance(coeffs)(source.peer)
    center = mappedPt
  }

}

object Landmarks {
  case class LandmarksChanged(source: AnyRef) extends Event
}

trait Landmarks[L <: Landmark] extends MutableObjectContainer[L] with Publisher with Saveable {
  override def addAll(lms: Seq[L]) = {
    super.addAll(lms)
    publish(Landmarks.LandmarksChanged(this))
  }
  
  override def remove(lm: L) = {
    val changed = super.remove(lm)
    if (changed) publish(Landmarks.LandmarksChanged(this))
    changed
  }
  
  override def saveToFile(file: File) : Try[Unit] = Try {
    val seq = children.map{ lm => (lm.name, lm.peer)}.toIndexedSeq
    LandmarkIO.writeLandmarks[ThreeD](file, seq)
  }
} 

abstract class DisplayableLandmarks(theObject: ThreeDObject) extends SceneTreeObjectContainer[DisplayableLandmark] with Landmarks[DisplayableLandmark] with Radius with Colorable {
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

class StaticLandmarks(theObject: ThreeDObject) extends DisplayableLandmarks(theObject) {
  lazy val nameGenerator: NameGenerator = NameGenerator.defaultGenerator
  def addAt(position: Point3D) = {
    val lm = new StaticLandmark(position, this)
    lm.name = nameGenerator.nextName
    add(lm)
  }
}

class ReferenceLandmarks(val shapeModel: ShapeModel) extends Landmarks[ReferenceLandmark] {
  lazy val nameGenerator: NameGenerator = NameGenerator.defaultGenerator
  def create(template: ReferenceLandmark): ReferenceLandmark = {
    val lm = new ReferenceLandmark(template.peer)
    lm.name = template.name
    add(lm)
    lm
  }
  def create(peer: Point3D): ReferenceLandmark = {
    val lm = new ReferenceLandmark(peer)
    lm.name = nameGenerator.nextName
    add(lm)
    lm
  }
}

class MoveableLandmarks(val instance: ShapeModelInstance) extends DisplayableLandmarks(instance) {
  def addAt(position: Point3D) = ??? // this is on purpose, it's not used.
  val ref = instance.parent.parent.landmarks

  listenTo(ref)

  reactions += {
    case Landmarks.LandmarksChanged(s) => {
      if (s eq ref) {
        syncWithModel
      }
    }
  }

  syncWithModel

  def syncWithModel = {
    var changed = false
    _children.length until ref.children.length foreach { i =>
      changed = true
      add(new MoveableLandmark(this, ref(i)))
    }
    if (changed) {
      publish(SceneTreeObject.ChildrenChanged(this))
    }
  }
}