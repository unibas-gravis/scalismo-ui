package org.statismo.stk.ui

import scala.collection.immutable

object Perspective {
  //def defaultPerspective(scene: Scene) = new SlicerAltPerspective(None)(scene)
  def defaultPerspective(scene: Scene) = new SingleViewportPerspective(None)(scene)
}

abstract class Perspective(template: Option[Perspective]) extends Nameable {
  final lazy val viewports: immutable.Seq[Viewport] = createViewports()

  protected def createViewports(): immutable.Seq[Viewport]

  val factory: PerspectiveFactory
  name = factory.name

  def reuseOrInstantiate3D(perspective: Option[Perspective], count: Int)(implicit scene: Scene): immutable.Seq[ThreeDViewport] = {
    val existing = perspective match {
      case Some(p) => p.viewports.filter(vp => vp.isInstanceOf[ThreeDViewport]).asInstanceOf[immutable.Seq[ThreeDViewport]]
      case None => Nil
    }
    if (existing.length < count) {
      val created = List.fill(count - existing.length)(new ThreeDViewport(scene))
      immutable.Seq(existing, created).flatten
    } else existing.take(count)
  }

  def reuseOrInstantiate2D(perspective: Option[Perspective], axis: Axis.Value, count: Int)(implicit scene: Scene): immutable.Seq[TwoDViewport] = {
    val existing = perspective match {
      case Some(p) => p.viewports.filter(vp => vp.isInstanceOf[TwoDViewport]).asInstanceOf[immutable.Seq[TwoDViewport]].filter(vp => vp.axis == axis)
      case None => Nil
    }
    if (existing.length < count) {
      val created = List.fill(count - existing.length)(new TwoDViewport(scene, axis))
      immutable.Seq(existing, created).flatten
    } else existing.take(count)
  }
}

object Perspectives {
  lazy val availablePerspectives: immutable.Seq[PerspectiveFactory] = immutable.Seq(SingleViewportPerspective, TwoViewportsPerspective, FourViewportsPerspective, /*SlicerPerspective,*/ OrthogonalSlicesPerspective, XOnlyPerspective, YOnlyPerspective, ZOnlyPerspective)
}

trait PerspectiveFactory {
  val name: String

  def apply()(implicit scene: Scene): Perspective
}


object SingleViewportPerspective extends PerspectiveFactory {
  override lazy val name = "Single 3D Window"

  override def apply()(implicit scene: Scene): Perspective = new SingleViewportPerspective(Some(scene.perspective))
}

class SingleViewportPerspective(template: Option[Perspective])(implicit val scene: Scene) extends Perspective(template) {
  override lazy val factory = SingleViewportPerspective

  protected override def createViewports() = {
    val only = reuseOrInstantiate3D(template, 1).head
    only.name = "3D View"
    List(only)
  }
}


object TwoViewportsPerspective extends PerspectiveFactory {
  override lazy val name = "Two 3D Windows"

  override def apply()(implicit scene: Scene): Perspective = new TwoViewportsPerspective(Some(scene.perspective))
}

class TwoViewportsPerspective(template: Option[Perspective])(implicit val scene: Scene) extends Perspective(template) {
  override lazy val factory = TwoViewportsPerspective

  protected override def createViewports() = {
    val vp = reuseOrInstantiate3D(template, 2)
    vp(0).name = "Left"
    vp(1).name = "Right"
    vp
  }
}


object FourViewportsPerspective extends PerspectiveFactory {
  override lazy val name = "Four 3D Windows"

  override def apply()(implicit scene: Scene): Perspective = new FourViewportsPerspective(Some(scene.perspective))
}

class FourViewportsPerspective(template: Option[Perspective])(implicit scene: Scene) extends Perspective(template) {
  override lazy val factory = FourViewportsPerspective

  protected override def createViewports() = {
    val vp = reuseOrInstantiate3D(template, 4)
    vp(0).name = "One"
    vp(1).name = "Two"
    vp(2).name = "Three"
    vp(3).name = "Four"
    vp
  }
}


object OrthogonalSlicesPerspective extends PerspectiveFactory {
  override lazy val name = "Orthogonal Slices"

  override def apply()(implicit scene: Scene): Perspective = new OrthogonalSlicesPerspective(Some(scene.perspective))
}

class OrthogonalSlicesPerspective(template: Option[Perspective])(implicit scene: Scene) extends Perspective(template) {
  override lazy val factory = OrthogonalSlicesPerspective

  protected override def createViewports() = {
    val threeD = reuseOrInstantiate3D(template, 1).head
    val x = reuseOrInstantiate2D(template, Axis.X, 1).head
    val y = reuseOrInstantiate2D(template, Axis.Y, 1).head
    val z = reuseOrInstantiate2D(template, Axis.Z, 1).head
    threeD.name = "3D"
    Seq(x, y, z).foreach(v => v.name = v.axis.toString)
    immutable.Seq(threeD, x, y, z)
  }
}


object XOnlyPerspective extends PerspectiveFactory {
  override lazy val name = "X Slice"

  override def apply()(implicit scene: Scene): Perspective = new XOnlyPerspective(Some(scene.perspective))
}

class XOnlyPerspective(template: Option[Perspective])(implicit val scene: Scene) extends Perspective(template) {
  override lazy val factory = XOnlyPerspective

  protected override def createViewports() = {
    val only = reuseOrInstantiate2D(template, Axis.X, 1).head
    only.name = "X"
    List(only)
  }
}


object YOnlyPerspective extends PerspectiveFactory {
  override lazy val name = "Y Slice"

  override def apply()(implicit scene: Scene): Perspective = new YOnlyPerspective(Some(scene.perspective))
}

class YOnlyPerspective(template: Option[Perspective])(implicit val scene: Scene) extends Perspective(template) {
  override lazy val factory = YOnlyPerspective

  protected override def createViewports() = {
    val only = reuseOrInstantiate2D(template, Axis.Y, 1).head
    only.name = "Y"
    List(only)
  }
}


object ZOnlyPerspective extends PerspectiveFactory {
  override lazy val name = "Z Slice"

  override def apply()(implicit scene: Scene): Perspective = new ZOnlyPerspective(Some(scene.perspective))
}

class ZOnlyPerspective(template: Option[Perspective])(implicit val scene: Scene) extends Perspective(template) {
  override lazy val factory = ZOnlyPerspective

  protected override def createViewports() = {
    val only = reuseOrInstantiate2D(template, Axis.Z, 1).head
    only.name = "Z"
    List(only)
  }
}

