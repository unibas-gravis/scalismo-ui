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
  def Name: String
  def apply(scene: Scene): Perspective
}

object SingleViewportPerspective extends PerspectiveFactory {
  val Name = "Single 3D Window"
  def apply(scene: Scene): Perspective = new SingleViewportPerspective(scene)
}

class SingleViewportPerspective(val scene: Scene) extends Perspective {
  override val factory = SingleViewportPerspective
  name = factory.Name
  def createViewports = Seq(new ThreeDViewport(scene, Some("3D View")))
}

object TwoViewportsPerspective extends PerspectiveFactory {
  val Name = "Two 3D Windows"
  def apply(scene: Scene): Perspective = new TwoViewportsPerspective(scene)
}

class TwoViewportsPerspective(val scene: Scene) extends Perspective {
  override val factory = TwoViewportsPerspective
  name = factory.Name
  def createViewports = Seq(new ThreeDViewport(scene, Some("Left")), new ThreeDViewport(scene, Some("Right")))
}

object FourViewportsPerspective extends PerspectiveFactory {
  val Name = "Four 3D Windows"
  def apply(scene: Scene): Perspective = new FourViewportsPerspective(scene)
}

class FourViewportsPerspective(val scene: Scene) extends Perspective {
  override val factory = FourViewportsPerspective
  name = factory.Name
  def createViewports = Seq(new ThreeDViewport(scene, Some("One")), new ThreeDViewport(scene, Some("Two")), new ThreeDViewport(scene, Some("Three")), new ThreeDViewport(scene, Some("Four")))
}

object SlicerPerspective extends PerspectiveFactory {
  val Name = "Slicer"
  def apply(scene: Scene): Perspective = new SlicerPerspective(scene)
}

class SlicerPerspective(val scene: Scene) extends Perspective {
  override val factory = SlicerPerspective
  name = factory.Name
  def createViewports = Seq(new ThreeDViewport(scene, Some("3D")), new ThreeDViewport(scene, Some("X")), new ThreeDViewport(scene, Some("Y")), new ThreeDViewport(scene, Some("Z")))
}