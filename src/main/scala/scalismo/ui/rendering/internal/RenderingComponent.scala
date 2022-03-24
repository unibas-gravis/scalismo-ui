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

package scalismo.ui.rendering.internal

import com.jogamp.opengl.{GLAutoDrawable, GLCapabilities, GLEventListener, GLProfile}
import com.jogamp.opengl.awt.GLJPanel

import java.util.concurrent.locks.ReentrantLock
import scalismo.ui.view.ViewportPanel
import vtk._
import vtk.rendering.jogl.vtkJoglPanelComponent

/**
 * A minor extension of vtkJoglPanelComponent.
 *
 * Note:  This component is here mainly for historical purposes. When Scalismo used
 * jogl 2.2 and older, parts of the functionality was reimplemented. With jogl 2.4 this
 * does not seem to be strictly necessary anymore. We keep the rendererState here, until we
 * have a good idea how to refactor tat.
 */
class RenderingComponent(viewport: ViewportPanel) extends vtk.rendering.jogl.vtkJoglPanelComponent {

  val rendererState = new RendererStateImplementation(renderer, viewport)
  def render() = super.Render()
}
