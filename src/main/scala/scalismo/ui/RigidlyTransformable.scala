package scalismo.ui

import scalismo.geometry._3D
import scalismo.registration.RigidTransformation

import scala.swing.event.Event

object RigidlyTransformable {
  case class RigidTransformationChanged(source: RigidlyTransformable) extends Event
}

trait RigidlyTransformable extends EdtPublisher {
  private var _transformation: Option[RigidTransformation[_3D]] = None

  def rigidTransformation: Option[RigidTransformation[_3D]] = _transformation

  def rigidTransformation_=(newValue:Option[RigidTransformation[_3D]]) = {
    _transformation = newValue
    publishEdt(RigidlyTransformable.RigidTransformationChanged(this))
  }
}
