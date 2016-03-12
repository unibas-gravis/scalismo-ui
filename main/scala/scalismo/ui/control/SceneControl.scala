package scalismo.ui.control

import scalismo.ui.model.{ Renderable, Scene }
import scalismo.ui.view.{ ScalismoFrame, ViewportPanel }

/**
 * This class is in a bit of an awkward position, as it conceptually sits
 * somewhere between the model and the view. Essentially, it controls
 * properties of a scene, in a particular view. Such things can't go
 * inside the model package -- because a scene does not know anything about
 * frames etc. But they don't really belong into the view package either,
 * because that one contains Swing implementations. So for now, there's the
 * control package.
 */
class SceneControl(val frame: ScalismoFrame, val scene: Scene) {
  val slicingPosition = new SlicingPosition(scene, frame)
  val nodeVisibility = new NodeVisibility(frame)

  def initialize(): Unit = {
    slicingPosition.initialize()
    nodeVisibility.initialize()
  }

  def renderablesFor(viewport: ViewportPanel): List[Renderable] = {
    scene.renderables.filter(r => nodeVisibility.isVisible(r, viewport))
  }
}
