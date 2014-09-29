package org.statismo.stk.ui.vtk

import org.statismo.stk.core.utils.{Benchmark, ImageConversion}
import org.statismo.stk.ui.vtk.VtkContext.RenderRequest

import org.statismo.stk.ui.{Axis, Image3D}

class ImageActor3D(source: Image3D[_])(implicit viewport: VtkViewport) extends RenderableActor {
  val points = Caches.ImageCache.getOrCreate(source, ImageConversion.image3DTovtkStructuredPoints(source.asFloatImage))

  val x = ImageActor2D(source, points, Axis.X)
  val y = ImageActor2D(source, points, Axis.Y)
  val z = ImageActor2D(source, points, Axis.Z)

  deafTo(this)
  listenTo(x,y,z)

  reactions += {
    // simply forward render requests
    case RenderRequest(ctx, imm) =>
      publishEdt(RenderRequest(this, immediately = imm))
  }

  override lazy val vtkActors: Seq[ImageActor2D] = Seq(x, y, z)

  override def onDestroy() = this.synchronized {
    deafTo(viewport.interactor, source.scene, x, y, z)
    super.onDestroy()
    vtkActors.foreach(_.onDestroy())
  }

  override def currentBoundingBox = VtkUtils.bounds2BoundingBox(points.GetBounds())
}