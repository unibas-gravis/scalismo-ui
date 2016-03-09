package scalismo.ui.view

import scalismo.ui.event.{ Event, ScalismoPublisher }
import scalismo.ui.settings.GlobalSettings
import scalismo.ui.util.EdtUtil
import scalismo.ui.view.perspective.{ Perspective, PerspectiveFactory }

import scala.swing.BorderPanel

object PerspectivesPanel {

  object event {

    case class PerspectiveChanged(panel: PerspectivesPanel) extends Event

  }

}

class PerspectivesPanel(val frame: ScalismoFrame) extends BorderPanel with ScalismoPublisher {

  // because of issues with VTK rendering, we can't just create and dispose render windows
  // as needed (the program would crash at some point in time). Therefore, we instantiate
  // perspectives on demand, but keep them around in case they are used again later.
  private val cards = new CardPanel

  def perspective_=(factory: PerspectiveFactory): Unit = {
    if (factory.perspectiveName != cards.currentId) {
      if (cards.currentId != CardPanel.NoCard) {
        perspectiveInstance.viewports.foreach(_.setAttached(false))
      }

      if (!cards.contains(factory.perspectiveName)) {
        // we're extra careful with the instantiation, ensuring that it really happens on the EDT
        cards.add(EdtUtil.onEdtWait(factory.instantiate(frame)))
      }
      // save currently used perspective
      GlobalSettings.set(GlobalSettings.Keys.PerspectiveName, factory.perspectiveName)

      cards.show(factory.perspectiveName)
      cards.setActiveCards(List(cards.currentComponent))

      perspectiveInstance.viewports.foreach(_.setAttached(true))

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
