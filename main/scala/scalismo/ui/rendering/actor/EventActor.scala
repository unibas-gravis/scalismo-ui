package scalismo.ui.rendering.actor

import scalismo.ui.rendering.RendererContext
import vtk.vtkActor

import scala.collection.mutable.ArrayBuffer
import scala.swing.Publisher

trait EventActor extends vtkActor with RendererContext {

  private lazy val listening: ArrayBuffer[Publisher] = new ArrayBuffer

  override def listenTo(ps: Publisher*): Unit = {
    listening ++= ps
    super.listenTo(ps: _*)
  }

  override def deafTo(ps: Publisher*): Unit = {
    listening --= ps
    super.deafTo(ps: _*)
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