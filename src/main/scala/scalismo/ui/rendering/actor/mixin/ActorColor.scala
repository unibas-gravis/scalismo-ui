package scalismo.ui.rendering.actor.mixin

import scalismo.ui.model.properties.{ ColorProperty, NodeProperty }
import scalismo.ui.rendering.actor.{ ActorEvents, SingleActor }
import scalismo.ui.rendering.util.VtkUtil

trait ActorColor extends SingleActor with ActorEvents {
  def color: ColorProperty

  listenTo(color)

  reactions += {
    case NodeProperty.event.PropertyChanged(p) if p eq color => setAppearance()
  }

  private def setAppearance() = {
    GetProperty().SetColor(VtkUtil.colorToArray(color.value))
    actorChanged()
  }

  setAppearance()

}
