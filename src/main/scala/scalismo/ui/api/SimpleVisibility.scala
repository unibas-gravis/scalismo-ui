package scalismo.ui.api

import scalismo.ui.model.capabilities.RenderableSceneNode

/**
  * Trait to be mixed with ObjectView instances in order to provide visibility changing functionality
  *
  */

sealed trait Viewport {
  val name: String
}

case object xView extends Viewport {
  override val name = "X"
}

case object yView extends Viewport {
  override val name = "Y"
}

case object zView extends Viewport {
  override val name = "Z"
}

case object _3DLeft extends Viewport {
  override val name = "Left"
}

case object _3DRight extends Viewport {
  override val name = "Right"
}

case object _3D extends Viewport {
  override val name = "3D"
}

object Viewport {

  val all = Seq(xView, yView, zView, _3D, _3DLeft, _3DRight)
  val none = Seq()
  val xOnly: Seq[Viewport] = Seq(xView)
  val yOnly: Seq[Viewport] = Seq(yView)
  val zOnly: Seq[Viewport] = Seq(zView)
  val _2dOnly: Seq[Viewport] = Seq(xView, yView, zView)
  val _3dOnly: Seq[Viewport] = Seq(_3D, _3DRight, _3DLeft)
  val _3dLeft: Seq[Viewport] = Seq(_3DLeft)
  val _3dRight: Seq[Viewport] = Seq(_3DRight)
}

sealed trait VisibilityConfig {
  val views: Seq[Viewport]
}


trait SimpleVisibility {
  self: ObjectView =>

  private def setVisible(isVisible: Boolean, viewportName: String): Unit = {
    val viewPort = self.frame.perspective.viewports.filter(_.name == viewportName).head
    val visib = self.frame.sceneControl.nodeVisibility
    visib.setVisibility(peer.asInstanceOf[RenderableSceneNode], viewPort, isVisible)
  }

  /**
    * Sets the node visible to all views in the config, and invisible in others
    **/
  def visible_=(config: Seq[Viewport]) = {
    config.foreach(v => setVisible(true, v.name))

    //set invisible in other viewports
    val viewsNotInConfig = self.frame.perspective.viewports.filterNot(v => config.exists(_.name == v.name))
    viewsNotInConfig.foreach(v => setVisible(false, v.name))
  }

}