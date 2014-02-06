package org.statismo.stk.ui

import org.statismo.stk.core.geometry.Point3D
import java.awt.Color

class VirtualLandmark(val pointIndex: Int) extends Nameable

class DisplayableLandmark(container: DisplayableLandmarks) extends Nameable with Displayable with SphereLike {
  override lazy val parent = container
  color = container.color
  opacity = container.opacity
  radius = container.radius
}

class StaticLandmark(initialCenter: Point3D, container: StaticLandmarks) extends DisplayableLandmark(container) {
  center = initialCenter
}

class MoveableLandmark(container: MoveableLandmarks, source: VirtualLandmark) extends DisplayableLandmark(container) {
  override lazy val parent = container

  name = source.name
  listenTo(container.instance.meshRepresentation, source)

  reactions += {
    case Mesh.GeometryChanged(m) => setCenter
    case Nameable.NameChanged(n) => {
      if (n == source) {
        this.name = source.name
      } else if (n == this) {
        source.name = this.name
      }
    }
  }

  setCenter

  def setCenter = {
    val mesh = container.instance.meshRepresentation.triangleMesh
    val pt = mesh.meshPoints(source.pointIndex).asInstanceOf[Point3D]
    center = pt
  }
}

abstract class DisplayableLandmarks(theObject: ThreeDObject) extends SceneTreeObjectContainer[DisplayableLandmark] with Radius with Colorable {
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

class MoveableLandmarks(val instance: ShapeModelInstance) extends DisplayableLandmarks(instance) {
  def addAt(position: Point3D) = ??? // this is on purpose, it's not used.
  val model = instance.parent.parent

  listenTo(model)

  reactions += {
    case ShapeModel.LandmarksAdded(m) => syncWithModel
  }

  syncWithModel

  def syncWithModel = {
    // TODO: we can only handle addition for now, not removal.
    var changed = false
    _children.length until model.landmarks.length foreach { i =>
      changed = true
      _children += new MoveableLandmark(this, model.landmarks(i))
    }
    if (changed) {
      publish(SceneTreeObject.ChildrenChanged(this))
    }
  }
}