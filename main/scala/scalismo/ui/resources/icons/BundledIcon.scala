package scalismo.ui.resources.icons

import java.awt.Color

import jiconfont.icons.FontAwesome
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

  // toolbar or other general-purpose icons
  lazy val Reset = FontIcon.load(FontAwesome.UNDO)
  lazy val Screenshot = FontIcon.load(FontAwesome.CAMERA)
  lazy val Remove = FontIcon.load(awesome('\uf00d'))

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

}