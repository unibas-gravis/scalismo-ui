package org.statismo.stk.ui

object Perspective {
  def defaultPerspective(scene: Scene) = new SingleViewportPerspective(scene)
}

trait Perspective extends Nameable {
  final lazy val viewports: Seq[Viewport] = createViewports

  def createViewports: Seq[Viewport]

  def factory: PerspectiveFactory
}

object Perspectives {
  lazy val availablePerspectives: Seq[PerspectiveFactory] = Seq(SingleViewportPerspective, TwoViewportsPerspective, FourViewportsPerspective, SlicerPerspective)
}

trait PerspectiveFactory {
  def name: String

  def apply(scene: Scene): Perspective
}

object SingleViewportPerspective extends PerspectiveFactory {
  val name = "Single 3D Window"

  def apply(scene: Scene): Perspective = new SingleViewportPerspective(scene)
}

class SingleViewportPerspective(val scene: Scene) extends Perspective {
  override val factory = SingleViewportPerspective
  name = factory.name

  def createViewports = Seq(new ThreeDViewport(scene, Some("3D View")))
}

object TwoViewportsPerspective extends PerspectiveFactory {
  val name = "Two 3D Windows"

  def apply(scene: Scene): Perspective = new TwoViewportsPerspective(scene)
}

class TwoViewportsPerspective(val scene: Scene) extends Perspective {
  override val factory = TwoViewportsPerspective
  name = factory.name

  def createViewports = Seq(new ThreeDViewport(scene, Some("Left")), new ThreeDViewport(scene, Some("Right")))
}

object FourViewportsPerspective extends PerspectiveFactory {
  val name = "Four 3D Windows"

  def apply(scene: Scene): Perspective = new FourViewportsPerspective(scene)
}

class FourViewportsPerspective(val scene: Scene) extends Perspective {
  override val factory = FourViewportsPerspective
  name = factory.name

  def createViewports = Seq(new ThreeDViewport(scene, Some("One")), new ThreeDViewport(scene, Some("Two")), new ThreeDViewport(scene, Some("Three")), new ThreeDViewport(scene, Some("Four")))
}

object SlicerPerspective extends PerspectiveFactory {
  val name = "Slicer"

  def apply(scene: Scene): Perspective = new SlicerPerspective(scene)
}

class SlicerPerspective(val scene: Scene) extends Perspective {
  override val factory = SlicerPerspective
  name = factory.name

  def createViewports = Seq(new ThreeDViewport(scene, Some("3D")), new TwoDViewport(scene, ThreeDImageAxis.X, Some("X")), new TwoDViewport(scene, ThreeDImageAxis.Y, Some("Y")), new TwoDViewport(scene, ThreeDImageAxis.Z, Some("Z")))
}