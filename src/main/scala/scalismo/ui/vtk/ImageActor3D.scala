package scalismo.ui.vtk

import scalismo.ui.vtk.VtkContext.RenderRequest
import scalismo.ui.{ Axis, Image3DView }

class ImageActor3D(source: Image3DView[_])(implicit viewport: VtkViewport) extends RenderableActor {
  val x = ImageActor2D(source, Axis.X)
  val y = ImageActor2D(source, Axis.Y)
  val z = ImageActor2D(source, Axis.Z)

  deafTo(this)
  listenTo(x, y, z)

  reactions += {
    // simply forward render requests
    case RenderRequest(ctx, imm) =>
      publishEdt(RenderRequest(this, immediately = imm))
  }

  override lazy val vtkActors: Seq[ImageActor2D] = Seq(x, y, z)

  override def onDestroy() = this.synchronized {
    deafTo(x, y, z)
    super.onDestroy()
    vtkActors.foreach(_.onDestroy())
  }

  /* all slices return the same bounding box (of the entire volume), so it doesn't matter which slice we take it from */
  override def currentBoundingBox = x.currentBoundingBox
}
