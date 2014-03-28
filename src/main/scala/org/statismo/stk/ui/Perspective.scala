package org.statismo.stk.ui

import scala.collection.immutable

object Perspective {
  def defaultPerspective(scene: Scene) = new SlicerPerspective(None)(scene)//SingleViewportPerspective(None)(scene)
}

abstract class Perspective(template: Option[Perspective]) extends Nameable {
  final lazy val viewports: immutable.Seq[Viewport] = createViewports()

  def createViewports(): immutable.Seq[Viewport]

  val factory: PerspectiveFactory
  name = factory.name

  def reuseOrInstantiate[A](existing: immutable.Seq[A], index: Int, instantiate: Scene => A)(implicit scene: Scene): A = {
    if (existing.length > index) {
      existing(index)
    } else {
      instantiate(scene)
    }
  }

}

object Perspectives {
  lazy val availablePerspectives: immutable.Seq[PerspectiveFactory] = immutable.Seq(SingleViewportPerspective, TwoViewportsPerspective, FourViewportsPerspective, SlicerPerspective)
}

trait PerspectiveFactory {
  val name: String

  def apply()(implicit scene: Scene): Perspective
}

object SingleViewportPerspective extends PerspectiveFactory {
  override lazy val name = "Single 3D Window"

  def apply()(implicit scene: Scene): Perspective = new SingleViewportPerspective(Some(scene.perspective))
}

class SingleViewportPerspective(template: Option[Perspective])(implicit val scene: Scene) extends Perspective(template) {
  override lazy val factory = SingleViewportPerspective

  override def createViewports() = {
    val name = "3D View"
    template match {
      case Some(p: Perspective) =>
        def instantiate(scene: Scene): ThreeDViewport = new ThreeDViewport(scene)
        val existing = p.viewports.filter(vp => vp.isInstanceOf[ThreeDViewport]).asInstanceOf[Seq[ThreeDViewport]].toList
        val only = reuseOrInstantiate(existing, 0, instantiate)
        only.name = name
        List(only)
      case None =>
        immutable.Seq(new ThreeDViewport(scene, Some(name)))
    }
  }
}

object TwoViewportsPerspective extends PerspectiveFactory {
  override lazy val name = "Two 3D Windows"

  def apply()(implicit scene: Scene): Perspective = new TwoViewportsPerspective(Some(scene.perspective))
}

class TwoViewportsPerspective(template: Option[Perspective])(implicit val scene: Scene) extends Perspective(template) {
  override lazy val factory = TwoViewportsPerspective

  override def createViewports() = {
    val leftName = "Left"
    val rightName = "Right"
    template match {
      case Some(p: Perspective) =>
        val existing = p.viewports.filter(vp => vp.isInstanceOf[ThreeDViewport]).asInstanceOf[Seq[ThreeDViewport]].toList
        def instantiate(scene: Scene): ThreeDViewport = new ThreeDViewport(scene)
        val left = reuseOrInstantiate(existing, 0, instantiate)
        left.name = leftName
        val right = reuseOrInstantiate(existing, 1, instantiate)
        right.name = rightName
        immutable.Seq(left, right)
      case None =>
        immutable.Seq(new ThreeDViewport(scene, Some(leftName)), new ThreeDViewport(scene, Some(rightName)))

    }
  }
}

object FourViewportsPerspective extends PerspectiveFactory {
  override lazy val name = "Four 3D Windows"

  def apply()(implicit scene: Scene): Perspective = new FourViewportsPerspective(Some(scene.perspective))
}

class FourViewportsPerspective(template: Option[Perspective])(implicit scene: Scene) extends Perspective(template) {
  override lazy val factory = FourViewportsPerspective

  override def createViewports() = {
    val nameOne = "One"
    val nameTwo = "Two"
    val nameThree = "Three"
    val nameFour = "Four"
    template match {
      case Some(p: Perspective) =>
        val existing = p.viewports.filter(vp => vp.isInstanceOf[ThreeDViewport]).asInstanceOf[Seq[ThreeDViewport]].toList
        def instantiate(scene: Scene): ThreeDViewport = new ThreeDViewport(scene)
        val one = reuseOrInstantiate(existing, 0, instantiate)
        val two = reuseOrInstantiate(existing, 1, instantiate)
        val three = reuseOrInstantiate(existing, 2, instantiate)
        val four = reuseOrInstantiate(existing, 3, instantiate)
        one.name = nameOne
        two.name = nameTwo
        three.name = nameThree
        four.name = nameFour
        immutable.Seq(one, two, three, four)
      case None =>
        immutable.Seq(new ThreeDViewport(scene, Some(nameOne)), new ThreeDViewport(scene, Some(nameTwo)), new ThreeDViewport(scene, Some(nameThree)), new ThreeDViewport(scene, Some(nameFour)))

    }
  }
}

object SlicerPerspective extends PerspectiveFactory {
  override lazy val name = "Slicer"

  def apply()(implicit scene: Scene): Perspective = new SlicerPerspective(Some(scene.perspective))
}

class SlicerPerspective(template: Option[Perspective])(implicit scene: Scene) extends Perspective(template) {
  override lazy val factory = SlicerPerspective

  override def createViewports() = {
    val nameOne = "3D"
    val nameTwo = "X"
    val nameThree = "Y"
    val nameFour = "Z"
    template match {
      case Some(p: Perspective) =>
        val existing = p.viewports.filter(vp => vp.isInstanceOf[ThreeDViewport]).asInstanceOf[Seq[ThreeDViewport]].toList
        def instantiate(scene: Scene): ThreeDViewport = new ThreeDViewport(scene)
        val one = reuseOrInstantiate(existing, 0, instantiate)
        one.name = nameOne
        val two = new TwoDViewport(scene, Axis.X, Some(nameTwo))
        val three = new TwoDViewport(scene, Axis.Y, Some(nameThree))
        val four = new TwoDViewport(scene, Axis.Z, Some(nameFour))
        immutable.Seq(one, two, three, four)
      case None =>
        immutable.Seq(new ThreeDViewport(scene, Some(nameOne)), new TwoDViewport(scene, Axis.X, Some(nameTwo)), new TwoDViewport(scene, Axis.Y, Some(nameThree)), new TwoDViewport(scene, Axis.Z, Some(nameFour)))
    }
  }
}