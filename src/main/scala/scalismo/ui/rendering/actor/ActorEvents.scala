package scalismo.ui.rendering.actor

import scalismo.ui.event.{ Event, ScalismoPublisher }

import scala.collection.mutable.ArrayBuffer
import scala.swing.Publisher

object ActorEvents {

  object event {

    case class ActorChanged(source: ActorEvents, didGeometryChange: Boolean) extends Event

  }

}

trait ActorEvents extends ScalismoPublisher {

  private lazy val listening: ArrayBuffer[Publisher] = new ArrayBuffer

  override def listenTo(ps: Publisher*): Unit = {
    listening ++= ps
    super.listenTo(ps: _*)
  }

  override def deafTo(ps: Publisher*): Unit = {
    listening --= ps
    super.deafTo(ps: _*)
  }

  //  def actorPropertyChanged(): Unit = {
  //    publishEvent(ActorEvents.event.ActorPropertyChanged(this))
  //  }
  //
  //  def actorGeometryChanged(): Unit = {
  //    publishEvent(ActorEvents.event.ActorGeometryChanged(this))
  //  }
  def actorChanged(didGeometryChange: Boolean = false) = {
    publishEvent(ActorEvents.event.ActorChanged(this, didGeometryChange))
  }

  /*
   * Invoked when this actor is destroyed. If you override this
   * method, you MUST invoke super.onDestroy() in your implementation.
   */
  def onDestroy(): Unit = {
    // we create an immutable copy of listening first, to prevent weird results
    deafTo(listening.toList: _*)
  }
}