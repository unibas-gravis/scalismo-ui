package org.statismo.stk.ui

import java.io.File
import scala.reflect.runtime.universe.TypeTag.Short
import scala.util.Try
import org.statismo.stk.core.image.DiscreteScalarImage3D
import org.statismo.stk.core.io.ImageIO
import scala.swing.event.Event
import org.statismo.stk.core.geometry.Point3D
import org.statismo.stk.core.common.ScalarValue

object ThreeDImage {
}

trait ThreeDImage extends ThreeDRepresentation with Saveable {
  def peer: DiscreteScalarImage3D[Short]

  override def saveToFile(file: File): Try[Unit] = {
    ImageIO.writeImage(peer, file)
  }
  
  val xPlane = new ThreeDImagePlane(this, ThreeDImageAxis.X)
  val yPlane = new ThreeDImagePlane(this, ThreeDImageAxis.Y)
  val zPlane = new ThreeDImagePlane(this, ThreeDImageAxis.Z)
  
  override lazy val children = Seq(xPlane, yPlane, zPlane)
  override lazy val saveableMetadata = StaticImage
  
  override def remove = {
    super.remove
    children.foreach(_.remove)
  }
}

object ThreeDImageAxis extends Enumeration {
  val X = Value
  val Y = Value
  val Z = Value
}

object ThreeDImagePlane {
  case class PositionChanged(source: ThreeDImagePlane) extends Event
}

class ThreeDImagePlane(val image: ThreeDImage, val axis: ThreeDImageAxis.Value) extends Displayable with Landmarkable with Removeable {
  override lazy val parent: ThreeDImage = image
  name = axis.toString()
  
  val minPosition = 0
  private var _maxPosition = 0
  def maxPosition = _maxPosition
  def maxPosition_=(newPosition: Int) = {
    _maxPosition = newPosition
  }
  
  private var _position = 0
  def position = _position
  def position_=(newPosition: Int) = {
    if (_position != newPosition) {
    	_position = newPosition
    	publish(ThreeDImagePlane.PositionChanged(this))
    }
  }
  
  def addLandmarkAt(point: Point3D) = {
    val obj = parent.parent.asInstanceOf[ThreeDRepresentations].parent
    obj.landmarks.addAt(point)
  }
  
  override val isCurrentlyRemoveable = false 
}