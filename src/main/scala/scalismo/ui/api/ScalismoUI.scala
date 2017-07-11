/*
 * Copyright (C) 2016  University of Basel, Graphics and Vision Research Group 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scalismo.ui.api

import java.awt.Image

import scalismo.ui.control.interactor.{ DefaultInteractor, Interactor }
import scalismo.ui.model.{ GroupNode, SceneNode }
import scalismo.ui.model.SceneNode.event.{ ChildAdded, ChildRemoved }
import scalismo.ui.model.capabilities.RenderableSceneNode
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.util.EdtUtil
import scalismo.ui.view.perspective.Perspective
import scalismo.ui.view.{ ScalismoFrame, ScalismoLookAndFeel }

class ScalismoUI(title: String) extends SimpleAPI with SimpleAPIDefaultImpl {

  private[ui] val fr = EdtUtil.onEdtWait {
    val frame = new ScalismoFrame()

    frame.setup(Array[String]())
    frame.pack()
    frame.visible = true

    frame.title = title
    frame.iconImage = ScalismoUI.appIcon
    frame
  }

  protected[api] val scene = fr.scene
  protected[api] val frame = fr

  override def setVisibility[V <: ObjectView](view: V, visibleViewports: Seq[Viewport]) = {

    def setVisible(isVisible: Boolean, viewportName: String): Unit = {
      frame.perspective.viewports.filter(_.name == viewportName).headOption.foreach { viewPort =>
        val visib = frame.sceneControl.nodeVisibility
        visib.setVisibility(view.peer.asInstanceOf[RenderableSceneNode], viewPort, isVisible)
      }
    }

    visibleViewports.foreach(v => setVisible(true, v.name))

    //set invisible in other viewports
    val viewsNotInConfig = frame.perspective.viewports.filterNot(v => visibleViewports.exists(_.name == v.name))
    viewsNotInConfig.foreach(v => setVisible(false, v.name))

  }

}

object ScalismoUI {

  def apply(title: String = ""): ScalismoUI = {
    scalismo.initialize()
    ScalismoLookAndFeel.initializeWith(ScalismoLookAndFeel.DefaultLookAndFeelClassName)
    new ScalismoUI(title)
  }

  // App Icon: add a unique icon to the app.
  // TODO: This should be a better and more distinguishable one, e.g., the logo without the background, or a more "stylized" version
  private[ScalismoUI] val appIcon: Image = BundledIcon.AppIcon
}

