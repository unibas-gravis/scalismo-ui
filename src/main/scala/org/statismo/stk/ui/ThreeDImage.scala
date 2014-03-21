package org.statismo.stk.ui

import java.io.File
import scala.util.Try
import org.statismo.stk.core.image.DiscreteScalarImage3D
import org.statismo.stk.core.io.ImageIO
import scala.swing.event.Event
import org.statismo.stk.core.geometry.Point3D
import org.statismo.stk.core.common.ScalarValue
import scala.reflect.ClassTag
import reflect.runtime.universe.TypeTag
import scala.language.existentials
import org.statismo.stk.ui.visualization.{VisualizationProvider, SimpleVisualizationFactory}

object ThreeDImage extends SimpleVisualizationFactory[ThreeDImage[_]]{
}

abstract class ThreeDImage[S](implicit val scalarValue: ScalarValue[S], implicit val tt: TypeTag[S], implicit val ct: ClassTag[S]) extends ThreeDRepresentation[ThreeDImage[S]] with Saveable {
  def peer: DiscreteScalarImage3D[S]

  override def visualizationProvider: VisualizationProvider[ThreeDImage[S]] = ???

  override def saveToFile(file: File): Try[Unit] = {
    ImageIO.writeImage(peer, file)
  }

  val xPlane = new ThreeDImagePlane(this, ThreeDImageAxis.X)
  val yPlane = new ThreeDImagePlane(this, ThreeDImageAxis.Y)
  val zPlane = new ThreeDImagePlane(this, ThreeDImageAxis.Z)

  override lazy val children = Seq(xPlane, yPlane, zPlane)
  override lazy val saveableMetadata = StaticImage

  override def remove() = {
    super.remove()
    children.foreach(_.remove())
  }

}

object ThreeDImageAxis extends Enumeration {
  val X = Value
  val Y = Value
  val Z = Value
}

object ThreeDImagePlane {

  case class PositionChanged(source: ThreeDImagePlane[_]) extends Event

}

class ThreeDImagePlane[A](val image: ThreeDImage[A], val axis: ThreeDImageAxis.Value)(implicit val ev: ScalarValue[A]) extends SceneTreeObject with Landmarkable with Removeable {
  override lazy val parent: ThreeDImage[A] = image
  name = axis.toString

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
    parent.threeDObject.landmarks.addAt(point)
  }

  override val isCurrentlyRemoveable = false
}