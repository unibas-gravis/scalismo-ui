package scalismo.ui

import scalismo.geometry.{ Point, _1D, _3D }
import scalismo.ui.visualization._
import scalismo.ui.visualization.props._

import scala.collection.immutable
import scala.collection.immutable.Seq

object PointCloud {

  class PointCloudRenderable3D(val source: PointCloud, override val color: ColorProperty, override val opacity: OpacityProperty, override val radiuses: RadiusesProperty[_1D]) extends Renderable with HasColorAndOpacity with HasRadiuses[_1D]

  object DefaultVisualizationStrategy extends VisualizationStrategy[PointCloud] {
    override def renderablesFor2D(targetObject: PointCloud): scala.Seq[Renderable] = Seq()

    override def renderablesFor3D(t: PointCloud): scala.Seq[Renderable] = Seq(new PointCloudRenderable3D(t, t.color, t.opacity, t.radiuses))
  }
}

trait PointCloud extends ThreeDRepresentation[PointCloud] with Landmarkable with HasColorAndOpacity with HasRadiuses[_1D] {

  override val color: ColorProperty = new ColorProperty(None)
  override val opacity: OpacityProperty = new OpacityProperty(None)
  override val radiuses: RadiusesProperty[_1D] = new RadiusesProperty(None)

  override def visualizationStrategy: VisualizationStrategy[PointCloud] = PointCloud.DefaultVisualizationStrategy

  def peer: immutable.IndexedSeq[Point[_3D]]
}
