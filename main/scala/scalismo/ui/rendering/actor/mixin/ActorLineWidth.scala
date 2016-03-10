package scalismo.ui.rendering.actor.mixin

import scalismo.ui.model.properties.{ LineWidthProperty, NodeProperty }
import scalismo.ui.rendering.actor.{ EventActor, SingleActor }

trait ActorLineWidth extends SingleActor with EventActor {
  def lineWidth: LineWidthProperty

  listenTo(lineWidth)

  reactions += {
    case NodeProperty.event.PropertyChanged(p) if p eq lineWidth => setAppearance()
  }

  private def setAppearance() = {
    GetProperty().SetLineWidth(lineWidth.value)
    requestRendering()
  }

  setAppearance()

}
