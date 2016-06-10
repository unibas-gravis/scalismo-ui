package scalismo.ui.rendering.actor.mixin

import scalismo.ui.model.properties.{ScalarRange, ScalarRangeProperty, HasScalarRange, NodeProperty}
import scalismo.ui.rendering.actor.{ ActorEvents, SinglePolyDataActor }

trait ActorScalarRange extends SinglePolyDataActor with ActorEvents { self : ActorScalarRange =>
  def scalarRange: ScalarRangeProperty

  listenTo(self.scalarRange)

  reactions += {
    case NodeProperty.event.PropertyChanged(v) if v.isInstanceOf[ScalarRangeProperty] => setAppearance()
  }

  private def setAppearance() = {
    mapper.SetScalarRange(scalarRange.value.cappedMinimum, scalarRange.value.cappedMaximum)
    mapper.Modified()
    actorChanged()
  }

  setAppearance()

}