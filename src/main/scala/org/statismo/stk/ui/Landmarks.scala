package org.statismo.stk.ui

import org.statismo.stk.core.geometry.Point3D
import java.awt.Color

trait Landmark extends Nameable {
  def position: Point3D
}

trait DisplayableLandmark extends Landmark with Displayable with SphereLike

case class StaticLandmark(initialCenter: Point3D, container: StaticLandmarks) extends DisplayableLandmark {
  override val position = center
  override lazy val parent = container
  color = container.color
  opacity = container.opacity
}

abstract class DisplayableLandmarks(theObject: ThreeDObject) extends SceneTreeObjectContainer[DisplayableLandmark] with Radius with Colorable {
  name = "Landmarks"
  override lazy val parent = theObject
  def addLandmarkAt(position: Point3D)
  
  override def opacity_=(newOpacity: Double) {
    super.opacity_=(newOpacity)
    children.foreach { c=>
      c.opacity = newOpacity
    }
  }
  
  override def color_=(newColor: Color) {
    super.color_=(newColor)
    children.foreach { c=>
      c.color = newColor
    }
  }
}

case class StaticLandmarks(theObject: ThreeDObject) extends DisplayableLandmarks(theObject) {
  def addLandmarkAt(position: Point3D) = {
    val lm = new StaticLandmark(position, this)
    add(lm)
  }
}