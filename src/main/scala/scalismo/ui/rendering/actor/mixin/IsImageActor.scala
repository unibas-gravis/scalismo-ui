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

package scalismo.ui.rendering.actor.mixin

import vtk.vtkActor

/**
 * This trait is only used as a flag to indicate that this actor is rendering images.
 * It is used to prioritize the order of adding actors to a renderer.
 *
 * See the [[scalismo.ui.rendering.RendererPanel]] implementation for more details.
 */
trait IsImageActor extends vtkActor {

}
