package org.statismo.stk.ui

import java.io.File

import org.statismo.stk.core.geometry.{ThreeD, Point}
import org.statismo.stk.core.io.MeshIO
import org.statismo.stk.core.mesh.TriangleMesh
import org.statismo.stk.ui.visualization._
import org.statismo.stk.ui.visualization.props._

import scala.collection.immutable
import scala.collection.immutable.Seq
import scala.swing.Reactor
import scala.swing.event.Event
import scala.util.Try

object PointCloud extends SimpleVisualizationFactory[PointCloud] {

  visualizations += Tuple2(Viewport.ThreeDViewportClassName, Seq(new Visualization3D(None)))
  visualizations += Tuple2(Viewport.TwoDViewportClassName, Seq(new NullVisualization[PointCloud]))

  class Visualization3D(from: Option[Visualization3D]) extends Visualization[PointCloud] with HasColorAndOpacity with HasRadius {
    override val color: ColorProperty = if (from.isDefined) from.get.color.derive() else new ColorProperty(None)
    override val opacity: OpacityProperty = if (from.isDefined) from.get.opacity.derive() else new OpacityProperty(None)
    override val radius: RadiusProperty = if (from.isDefined) from.get.radius.derive() else new RadiusProperty(None)

    protected def createDerived() = new Visualization3D(Some(this))

    protected def instantiateRenderables(source: PointCloud) = {
      Seq(new PointCloudRenderable3D(source, color, opacity, radius))
    }
  }

  class PointCloudRenderable3D(val source: PointCloud, override val color: ColorProperty, override val opacity: OpacityProperty, override val radius: RadiusProperty) extends Renderable with HasColorAndOpacity with HasRadius

}


trait PointCloud extends ThreeDRepresentation[PointCloud] with Landmarkable {
  def peer: immutable.IndexedSeq[Point[ThreeD]]

  protected[ui] override def visualizationProvider: VisualizationProvider[PointCloud] = PointCloud
}