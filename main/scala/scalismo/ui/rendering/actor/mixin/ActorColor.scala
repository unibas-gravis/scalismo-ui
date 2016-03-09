package scalismo.ui.rendering.actor.mixin

import scalismo.ui.model.properties.{ NodeProperty, ColorProperty }
import scalismo.ui.rendering.actor.{ DynamicActor, SingleActor }

trait ActorColor extends SingleActor with DynamicActor {
  def color: ColorProperty

  listenTo(color)

  reactions += {
    case NodeProperty.event.PropertyChanged(p) if p eq color => setAppearance()
  }

  private def setAppearance() = {
    val c = color.value.getColorComponents(null)
    GetProperty().SetColor(c(0), c(1), c(2))
    requestRendering()
  }

  setAppearance()

}
