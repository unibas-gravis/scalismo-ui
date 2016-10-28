package scalismo.ui.api

import scalismo.ui.model.capabilities.RenderableSceneNode

/**
 * Trait to be mixed with ObjectView instances in order to provide visibility changing functionality
 *
 */

sealed trait SimpleViewport

case object xView extends SimpleViewport
case object yView extends SimpleViewport
case object zView extends SimpleViewport
case object _3DLeft extends SimpleViewport
case object _3DRight extends SimpleViewport
case object _3D extends SimpleViewport

trait SimpleVisibility {
  self: ObjectView =>

  private def setVisible(isVisible: Boolean, viewportName: String): Unit = {
    val viewPort = self.frame.perspective.viewports.filter(_.name == viewportName).head
    val visib = self.frame.sceneControl.nodeVisibility
    visib.setVisibility(peer.asInstanceOf[RenderableSceneNode], viewPort, isVisible)
  }

  //def visible()

  def visible3D(isVisible: Boolean) = setVisible(isVisible, "3D")
  def visibleX(isVisible: Boolean) = setVisible(isVisible, "X")
  def visibleY(isVisible: Boolean) = setVisible(isVisible, "Y")
  def visibleZ(isVisible: Boolean) = setVisible(isVisible, "Z")

}