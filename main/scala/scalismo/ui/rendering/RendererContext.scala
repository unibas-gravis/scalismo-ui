package scalismo.ui.rendering

import scalismo.ui.event.{ Event, ScalismoPublisher }

object RendererContext {

  object event {

    case class RenderRequest(source: RendererContext) extends Event

  }

}

trait RendererContext extends ScalismoPublisher {
  def requestRendering(): Unit = {
    publishEvent(RendererContext.event.RenderRequest(this))
  }
}