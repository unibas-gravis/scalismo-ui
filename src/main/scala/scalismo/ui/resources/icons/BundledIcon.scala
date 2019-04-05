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

import java.awt.{ Color, Image }

import javax.swing.ImageIcon
import jiconfont.icons.{ Elusive, Entypo, FontAwesome }
import scalismo.ui.resources.icons.FontIcon.awesome
import scalismo.ui.resources.icons.png.PngIconResource

object BundledIcon {

  // This is an image (as opposed to an icon), because that's what Java Swing wants.
  // It's a 128x128 image that should hopefully be scaled according to the platform.
  lazy val AppIcon: Image = PngIconResource.load("app-icon.png").getImage

  lazy val Fallback: FontIcon = FontIcon.load(FontAwesome.BOLT)

  // this is relatively heavy, and seldomly needed, so we don't create a val.
  def Logo: ImageIcon = PngIconResource.load("logo.png")

  // basic icons
  lazy val Information: FontIcon = FontIcon.load(FontAwesome.INFO_CIRCLE, color = Color.BLUE.darker())
  lazy val Warning: FontIcon = FontIcon.load(FontAwesome.EXCLAMATION_TRIANGLE, color = Color.ORANGE)
  lazy val Error: FontIcon = FontIcon.load(FontAwesome.TIMES_CIRCLE, color = Color.RED.darker())
  lazy val Question: FontIcon = FontIcon.load(FontAwesome.QUESTION_CIRCLE, color = Color.BLUE.darker())

  // toolbar, menu, or other general-purpose icons
  lazy val Center: FontIcon = FontIcon.load(FontAwesome.DOT_CIRCLE_O)
  lazy val Smiley: FontIcon = FontIcon.load(FontAwesome.SMILE_O)
  lazy val Reset: FontIcon = FontIcon.load(FontAwesome.UNDO)
  lazy val Screenshot: FontIcon = FontIcon.load(FontAwesome.CAMERA)
  lazy val Remove: FontIcon = FontIcon.load(awesome('\uf00d'))
  lazy val Save: FontIcon = FontIcon.load(awesome('\uf0c7'))
  lazy val Load: FontIcon = FontIcon.load(FontAwesome.FILE_O)
  lazy val Name: FontIcon = FontIcon.load(FontAwesome.FONT)
  lazy val Exit: FontIcon = FontIcon.load(Entypo.LOGOUT)
  lazy val Scale: FontIcon = FontIcon.load(Entypo.RESIZE_FULL)
  lazy val Perspective: FontIcon = FontIcon.load(Elusive.GLASSES)
  lazy val Visible: FontIcon = FontIcon.load(Elusive.EYE_OPEN)
  lazy val Invisible: FontIcon = FontIcon.load(Elusive.EYE_CLOSE)
  lazy val Background: FontIcon = FontIcon.load(Entypo.ADJUST)

  // general SceneNode icons
  lazy val Scene: FontIcon = FontIcon.load(FontAwesome.HOME)
  lazy val Group: FontIcon = FontIcon.load(FontAwesome.CUBES)
  lazy val FolderOpen: FontIcon = FontIcon.load(FontAwesome.FOLDER_OPEN_O)
  lazy val FolderClosed: FontIcon = FontIcon.load(FontAwesome.FOLDER_O)

  // particular SceneNode classes
  lazy val Mesh: FontIcon = FontIcon.load(FontAwesome.DIAMOND)
  lazy val PointCloud: FontIcon = FontIcon.load(awesome('\uf1e3'))
  lazy val Landmark: FontIcon = FontIcon.load(FontAwesome.CROSSHAIRS)
  lazy val Transformation: FontIcon = FontIcon.load(FontAwesome.ARROW_CIRCLE_RIGHT)
  lazy val Image: FontIcon = FontIcon.load(FontAwesome.PICTURE_O)

}