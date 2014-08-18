package org.statismo.stk.ui

import org.statismo.stk.core.geometry.Point3D
import scala.swing.event.Event
import org.statismo.stk.ui.Repositionable.Amount

object Repositionable {

  case class CurrentPositionChanged(source: Repositionable) extends Event

  object Amount extends Enumeration {
    val Small, Medium, Large = Value

    def valueToFloat(`val`: Value): Float = {
      `val` match {
        case Small => 0.1f
        case Medium => 1.0f
        case Large => 10.0f
      }
    }
  }

}

trait Repositionable extends EdtPublisher {
  def getCurrentPosition: Point3D
  def increaseCurrentCoordinate(axis: Axis.Value, amount: Amount.Value): Unit
  def decreaseCurrentCoordinate(axis: Axis.Value, amount: Amount.Value): Unit
}

trait IndirectlyRepositionable extends Repositionable {
  /* Note: it is up to the implementation to react to the CurrentPositionChanged events of this object. */
  def directlyRepositionableObject: DirectlyRepositionable

  override def increaseCurrentCoordinate(axis: Axis.Value, amount: Amount.Value): Unit = {
    modifyCurrentPosition(axis, Amount.valueToFloat(amount))
  }

  override def decreaseCurrentCoordinate(axis: Axis.Value, amount: Amount.Value): Unit = {
    modifyCurrentPosition(axis, -1 * Amount.valueToFloat(amount))
  }

  private def modifyCurrentPosition(axis: Axis.Value, offset: Float): Unit = {
    val oldPosition = directlyRepositionableObject.getCurrentPosition
    val (ox, oy, oz) = (oldPosition.x, oldPosition.y, oldPosition.z)
    val (nx, ny, nz) = axis match {
      case Axis.X => (ox + offset, oy, oz)
      case Axis.Y => (ox, oy + offset, oz)
      case Axis.Z => (ox, oy, oz + offset)
    }
    directlyRepositionableObject.setCurrentPosition(Point3D(nx,ny,nz))
  }
}

trait DirectlyRepositionable extends IndirectlyRepositionable {
  override final def directlyRepositionableObject = this

  /* Note: it is up to the implementation to publishEdt() a CurrentPositionChanged event. */
  def setCurrentPosition(newPosition: Point3D): Unit
}