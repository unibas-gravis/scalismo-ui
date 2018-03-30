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
  val backgroundColor = new BackgroundColor()

  def initialize(): Unit = {
    slicingPosition.initialize()
    nodeVisibility.initialize()
  }

  def renderablesFor(viewport: ViewportPanel): List[Renderable] = {
    val sceneRenderables = scene.renderables.filter(r => nodeVisibility.isVisible(r, viewport))
    sceneRenderables ++ slicingPosition.renderablesFor(viewport)
  }
}
