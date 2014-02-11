package org.statismo.stk.ui

object Perspective {
  def defaultPerspective(scene: Scene) = new SingleViewportPerspective(scene) 
}

trait Perspective {
  def name: String
  final lazy val viewports: Seq[Viewport] = createViewports
  def createViewports: Seq[Viewport]
}

object Perspectives {
  lazy val availablePerspectives: Seq[PerspectiveFactory] = Seq(SingleViewportPerspective)
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
  override lazy val name = SingleViewportPerspective.Name
  def createViewports = Seq(new Viewport(scene, Some("3D View")))
}