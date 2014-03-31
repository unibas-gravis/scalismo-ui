package org.statismo.stk.ui.vtk

import org.statismo.stk.core.image.DiscreteScalarImage3D
import org.statismo.stk.core.utils.ImageConversion
import scala.collection.{mutable, immutable}
import org.statismo.stk.core.geometry.Point3D
import scala.swing.event.Event
import scala.swing.Swing

//import org.statismo.stk.ui.ThreeDImageAxis
//import org.statismo.stk.ui.ThreeDImagePlane
import vtk.{vtkImageDataGeometryFilter, vtkPolyDataMapper, vtkActor, vtkImagePlaneWidget}
import org.statismo.stk.ui.{Scene, Axis, Image3D, TwoDViewport}

class ImageActor3D(source: Image3D[_])(implicit viewport: VtkViewport) extends RenderableActor {
  val points = ImageConversion.image3DTovtkStructuredPoints(source.asFloatImage)

  def in(v: Double, min: Double, max: Double) = {
    min <= v && v <= max
  }

  val x = ImageActor2D(source, points, Axis.X)
  val y = ImageActor2D(source, points, Axis.Y)
  val z = ImageActor2D(source, points, Axis.Z)
  override lazy val vtkActors: Seq[ImageActor2D] = Seq(x,y,z)

  override def onDestroy() = this.synchronized {
    deafTo(viewport.interactor, source.scene)
    vtkActors.foreach(_.onDestroy())
    super.onDestroy()
  }

  override def currentBoundingBox = VtkUtils.bounds2BoundingBox(points.GetBounds())
}