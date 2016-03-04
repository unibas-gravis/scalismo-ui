package scalismo.ui.view

import java.awt.Color

import scalismo.ui.event.{ Event, ScalismoPublisher }
import scalismo.ui.util.EdtUtil
import scalismo.ui.view.perspective.{ Perspective, PerspectiveFactory }

import scala.swing.BorderPanel

object PerspectivesPanel {

  object event {

    case class PerspectiveChanged(panel: PerspectivesPanel) extends Event

  }

}

class PerspectivesPanel(val frame: ScalismoFrame) extends BorderPanel with ScalismoPublisher {

  private val cards = new CardPanel

  def perspective_=(factory: PerspectiveFactory): Unit = {
    if (factory.perspectiveName != cards.currentId) {
      if (!cards.contains(factory.perspectiveName)) {
        // we're extra careful with the instantiation, ensuring that it really happens on the EDT
        cards.add(EdtUtil.onEdtWait(factory.instantiate(frame)))
      }
      cards.show(factory.perspectiveName)
      cards.setActiveCards(List(cards.currentComponent))
      publishEvent(PerspectivesPanel.event.PerspectiveChanged(this))
    }
  }

  def perspective: PerspectiveFactory = {
    PerspectiveFactory.factories.find(_.perspectiveName == cards.currentId).get
  }

  def perspectiveInstance: Perspective = {
    cards.currentComponent.asInstanceOf[Perspective]
  }

  perspective = PerspectiveFactory.defaultPerspective

  layout(cards) = BorderPanel.Position.Center

}
