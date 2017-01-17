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

package scalismo.ui.model

/**
 * This is a generic trait for "anything that should
 * be picked up by the renderer". While most renderables
 * will normally directly originate from the scene
 * (like triangle meshes, images etc.), some information
 * (e.g., a bounding box) might not be directly contained
 * in the scene, but should still be visualized. This is
 * why this trait exists.
 */
trait Renderable extends AnyRef {

}
