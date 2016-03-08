package scalismo.ui.resources.icons

import javax.swing.Icon

import scalismo.ui.model._
import scalismo.ui.resources.icons.png.PngIconResource
import scalismo.ui.resources.icons.svg.SvgIconResource

object BundledIcon {
  lazy val Fallback = SvgIconResource.load("fallback.svg")

  // this is relatively heavy, and seldomly needed, so we don't create a val.
  def Logo = PngIconResource.load("logo.png")

  // basic icons
  lazy val Information = SvgIconResource.load("information.svg")
  lazy val Warning = SvgIconResource.load("warning.svg")
  lazy val Error = SvgIconResource.load("error.svg")
  lazy val Question = SvgIconResource.load("question.svg")

  // toolbar or other general-purpose icons
  lazy val Reset = SvgIconResource.load("reset.svg")
  lazy val Screenshot = SvgIconResource.load("screenshot.svg")

  // general SceneNode icons
  lazy val Scene = SvgIconResource.load("scene.svg")
  lazy val Group = SvgIconResource.load("group.svg")
  lazy val Collection = SvgIconResource.load("collection.svg")

  // particular SceneNode classes
  lazy val TriangleMesh = PngIconResource.load("mesh.png")

  /* FIXME:
   * This method is definitely at the wrong abstraction level.
   * It should be moved somewhere closer to the view.
   */
  def forNode(node: SceneNode): Icon = {
    val icon = node match {
      case scene: Scene => Scene
      case group: GroupNode => Group
      case triangleMeshNode: TriangleMeshNode => TriangleMesh
      case coll: SceneNodeCollection[_] => Collection
      case _ => Fallback
    }

    icon.standardSized()
  }
}