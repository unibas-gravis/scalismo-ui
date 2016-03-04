package scalismo.ui.view.perspective

import scalismo.ui.view.{ CardPanel, ScalismoFrame, ViewportPanel }

trait Perspective extends CardPanel.ComponentWithUniqueId {
  def factory: PerspectiveFactory
  def frame: ScalismoFrame
  def viewports: List[ViewportPanel]

  final override val uniqueId = factory.perspectiveName
}

object Perspective {

}
