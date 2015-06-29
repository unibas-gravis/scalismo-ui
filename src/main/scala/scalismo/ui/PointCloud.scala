package scalismo.ui

import scalismo.geometry.{ Point, _1D, _3D }
import scalismo.ui.visualization._
import scalismo.ui.visualization.props._

import scala.collection.immutable
import scala.collection.immutable.Seq

object PointCloud extends SimpleVisualizationFactory[PointCloud] {

  visualizations += Tuple2(Viewport.ThreeDViewportClassName, Seq(new Visualization3D(None)))
  visualizations += Tuple2(Viewport.TwoDViewportClassName, Seq(new NullVisualization[PointCloud]))

  class Visualization3D(from: Option[Visualization3D]) extends Visualization[PointCloud] with HasColorAndOpacity with HasRadiuses[_1D] {
    override val color: ColorProperty = if (from.isDefined) from.get.color.derive() else new ColorProperty(None)
    override val opacity: OpacityProperty = if (from.isDefined) from.get.opacity.derive() else new OpacityProperty(None)
    override val radiuses: RadiusesProperty[_1D] = if (from.isDefined) from.get.radiuses.derive() else new RadiusesProperty(None)

    protected def createDerived() = new Visualization3D(Some(this))

    protected def instantiateRenderables(source: PointCloud) = {
      Seq(new PointCloudRenderable3D(source, color, opacity, radiuses))
    }

    override val description: String = "Spheres"
  }

  class PointCloudRenderable3D(val source: PointCloud, override val color: ColorProperty, override val opacity: OpacityProperty, override val radiuses: RadiusesProperty[_1D]) extends Renderable with HasColorAndOpacity with HasRadiuses[_1D]

}

trait PointCloud extends ThreeDRepresentation[PointCloud] with Landmarkable {
  def peer: immutable.IndexedSeq[Point[_3D]]

  protected[ui] override def visualizationProvider: VisualizationProvider[PointCloud] = PointCloud
}