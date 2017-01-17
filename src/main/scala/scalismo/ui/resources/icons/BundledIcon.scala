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

package scalismo.ui.resources.icons

import java.awt.Color

import jiconfont.icons.{ Elusive, Entypo, FontAwesome }
import scalismo.ui.resources.icons.FontIcon.awesome
import scalismo.ui.resources.icons.png.PngIconResource

object BundledIcon {
  lazy val Fallback = FontIcon.load(FontAwesome.BOLT)

  // this is relatively heavy, and seldomly needed, so we don't create a val.
  def Logo = PngIconResource.load("logo.png")

  // basic icons
  lazy val Information = FontIcon.load(FontAwesome.INFO_CIRCLE, color = Color.BLUE.darker())
  lazy val Warning = FontIcon.load(FontAwesome.EXCLAMATION_TRIANGLE, color = Color.ORANGE)
  lazy val Error = FontIcon.load(FontAwesome.TIMES_CIRCLE, color = Color.RED.darker())
  lazy val Question = FontIcon.load(FontAwesome.QUESTION_CIRCLE, color = Color.BLUE.darker())

  // toolbar, menu, or other general-purpose icons
  lazy val Center = FontIcon.load(FontAwesome.DOT_CIRCLE_O)
  lazy val Smiley = FontIcon.load(FontAwesome.SMILE_O)
  lazy val Reset = FontIcon.load(FontAwesome.UNDO)
  lazy val Screenshot = FontIcon.load(FontAwesome.CAMERA)
  lazy val Remove = FontIcon.load(awesome('\uf00d'))
  lazy val Save = FontIcon.load(awesome('\uf0c7'))
  lazy val Load = FontIcon.load(FontAwesome.FILE_O)
  lazy val Name = FontIcon.load(FontAwesome.FONT)
  lazy val Exit = FontIcon.load(Entypo.LOGOUT)
  lazy val Scale = FontIcon.load(Entypo.RESIZE_FULL)
  lazy val Perspective = FontIcon.load(Elusive.GLASSES)
  lazy val Visible = FontIcon.load(Elusive.EYE_OPEN)
  lazy val Invisible = FontIcon.load(Elusive.EYE_CLOSE)

  // general SceneNode icons
  lazy val Scene = FontIcon.load(FontAwesome.HOME)
  lazy val Group = FontIcon.load(FontAwesome.CUBES)
  lazy val FolderOpen = FontIcon.load(FontAwesome.FOLDER_OPEN_O)
  lazy val FolderClosed = FontIcon.load(FontAwesome.FOLDER_O)

  // particular SceneNode classes
  lazy val Mesh = FontIcon.load(FontAwesome.DIAMOND)
  lazy val PointCloud = FontIcon.load(awesome('\uf1e3'))
  lazy val Landmark = FontIcon.load(FontAwesome.CROSSHAIRS)
  lazy val Transformation = FontIcon.load(FontAwesome.ARROW_CIRCLE_RIGHT)
  lazy val Image = FontIcon.load(FontAwesome.PICTURE_O)

}