package scalismo.ui

import scalismo.common.DiscreteScalarField
import scalismo.geometry.{ _1D, _3D }
import scalismo.ui.visualization._
import scalismo.ui.visualization.props._

import scala.collection.immutable.Seq

object ScalarField extends SimpleVisualizationFactory[ScalarField] {

  visualizations += Tuple2(Viewport.ThreeDViewportClassName, Seq(new Visualization3D(None)))
  visualizations += Tuple2(Viewport.TwoDViewportClassName, Seq(new NullVisualization[ScalarField]))

  class Visualization3D(from: Option[Visualization3D]) extends Visualization[ScalarField] with HasOpacity with HasRadiuses[_1D] {
    override val opacity: OpacityProperty = if (from.isDefined) from.get.opacity.derive() else new OpacityProperty(None)
    override val radiuses: RadiusesProperty[_1D] = if (from.isDefined) from.get.radiuses.derive() else new RadiusesProperty(None)

    protected def createDerived() = new Visualization3D(Some(this))

    protected def instantiateRenderables(source: ScalarField) = {
      Seq(new ScalarFieldRenderable3D(source, radiuses, opacity))
    }

    override val description: String = "Scalars"
  }

  class ScalarFieldRenderable3D(val source: ScalarField, override val radiuses: RadiusesProperty[_1D], override val opacity: OpacityProperty) extends Renderable with HasOpacity with HasRadiuses[_1D]

}

trait ScalarField extends ThreeDRepresentation[ScalarField] with Landmarkable {
  def peer: DiscreteScalarField[_3D, Float]

  protected[ui] override def visualizationProvider: VisualizationProvider[ScalarField] = ScalarField
}