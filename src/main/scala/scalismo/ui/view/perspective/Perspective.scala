package scalismo.ui.view.perspective

import scalismo.ui.view.util.CardPanel
import scalismo.ui.view.{ ScalismoFrame, ViewportPanel }

trait Perspective extends CardPanel.ComponentWithUniqueId {
  def factory: PerspectiveFactory

  def frame: ScalismoFrame

  def viewports: List[ViewportPanel]

  final override val uniqueId = factory.perspectiveName

  override def toString = factory.perspectiveName
}

object Perspective {

}
