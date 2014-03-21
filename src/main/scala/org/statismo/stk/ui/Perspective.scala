package org.statismo.stk.ui

import scala.collection.immutable

object Perspective {
  def defaultPerspective(scene: Scene) = new SingleViewportPerspective(scene, None)
}

abstract class Perspective(template: Option[Perspective]) extends Nameable {
  final lazy val viewports: immutable.Seq[Viewport] = createViewports()
  def createViewports(): immutable.Seq[Viewport]
  val factory: PerspectiveFactory
  name = factory.name
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

  def apply()(implicit scene: Scene): Perspective = new SingleViewportPerspective(scene, Some(scene.perspective))
}

class SingleViewportPerspective(val scene: Scene, template: Option[Perspective]) extends Perspective(template) {
  override lazy val factory = SingleViewportPerspective
  override def createViewports() = immutable.Seq(new ThreeDViewport(scene, Some("3D View")))
}

object TwoViewportsPerspective extends PerspectiveFactory {
  override lazy val name = "Two 3D Windows"

  def apply()(implicit scene: Scene): Perspective = new TwoViewportsPerspective(Some(scene.perspective))
}

class TwoViewportsPerspective(template: Option[Perspective])(implicit val scene: Scene) extends Perspective(template) {
  override lazy val factory = TwoViewportsPerspective
  override def createViewports() = {
    template match {
      case Some(p: Perspective) =>
        //FIXME: this (or a similar) method should probably be used for all perspectives
        def reuseOrCreate(existing: immutable.Seq[ThreeDViewport], index: Int)(implicit scene: Scene) :ThreeDViewport = {
          if (existing.length > index) {
            existing(index)
          } else {
            new ThreeDViewport(scene)
          }
        }
        val existing = p.viewports.filter(vp => vp.isInstanceOf[ThreeDViewport]).asInstanceOf[Seq[ThreeDViewport]].toList
        val left = reuseOrCreate(existing, 0)
        val right = reuseOrCreate(existing, 1)
        immutable.Seq(left, right)
      case None =>
        immutable.Seq(new ThreeDViewport(scene, Some("Left")), new ThreeDViewport(scene, Some("Right")))

    }
  }
}

object FourViewportsPerspective extends PerspectiveFactory {
  override lazy val name = "Four 3D Windows"

  def apply()(implicit scene: Scene): Perspective = new FourViewportsPerspective(scene, Some(scene.perspective))
}

class FourViewportsPerspective(val scene: Scene, template: Option[Perspective]) extends Perspective(template) {
  override lazy val factory = FourViewportsPerspective
  override def createViewports() = immutable.Seq(new ThreeDViewport(scene, Some("One")), new ThreeDViewport(scene, Some("Two")), new ThreeDViewport(scene, Some("Three")), new ThreeDViewport(scene, Some("Four")))
}

object SlicerPerspective extends PerspectiveFactory {
  override lazy val name = "Slicer"

  def apply()(implicit scene: Scene): Perspective = new SlicerPerspective(scene, Some(scene.perspective))
}

class SlicerPerspective(val scene: Scene, template: Option[Perspective]) extends Perspective(template) {
  override lazy val factory = SlicerPerspective
  override def createViewports() = immutable.Seq(new ThreeDViewport(scene, Some("3D")), new TwoDViewport(scene, ThreeDImageAxis.X, Some("X")), new TwoDViewport(scene, ThreeDImageAxis.Y, Some("Y")), new TwoDViewport(scene, ThreeDImageAxis.Z, Some("Z")))
}