package org.statismo.stk.ui.vtk

import org.statismo.stk.core.utils.ImageConversion

import org.statismo.stk.ui.{Axis, Image3D}

class ImageActor3D(source: Image3D[_])(implicit viewport: VtkViewport) extends RenderableActor {
  val points = ImageConversion.image3DTovtkStructuredPoints(source.asFloatImage)

  val x = ImageActor2D(source, points, Axis.X)
  val y = ImageActor2D(source, points, Axis.Y)
  val z = ImageActor2D(source, points, Axis.Z)
  override lazy val vtkActors: Seq[ImageActor2D] = Seq(x, y, z)

  override def onDestroy() = this.synchronized {
    deafTo(viewport.interactor, source.scene)
    super.onDestroy()
    vtkActors.foreach(_.onDestroy())
    points.Delete()
  }

  override def currentBoundingBox = VtkUtils.bounds2BoundingBox(points.GetBounds())
}