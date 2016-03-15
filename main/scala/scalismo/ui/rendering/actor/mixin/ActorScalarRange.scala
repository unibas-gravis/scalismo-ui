package scalismo.ui.rendering.actor.mixin

import scalismo.ui.model.properties.{ NodeProperty, ScalarRangeProperty }
import scalismo.ui.rendering.actor.{ ActorEvents, SinglePolyDataActor }

trait ActorScalarRange extends SinglePolyDataActor with ActorEvents {
  def scalarRange: ScalarRangeProperty

  listenTo(scalarRange)

  reactions += {
    case NodeProperty.event.PropertyChanged(p) if p eq scalarRange => setAppearance()
  }

  private def setAppearance() = {
    mapper.SetScalarRange(scalarRange.value.cappedMinimum, scalarRange.value.cappedMaximum)
    mapper.Modified()
    actorChanged()
  }

  setAppearance()

}