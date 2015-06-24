package scalismo.ui

import scalismo.common.DiscreteScalarField
import scalismo.geometry.{ _1D, _3D }
import scalismo.ui.visualization._
import scalismo.ui.visualization.props._

import scala.collection.immutable.Seq

object ScalarField {

  class ScalarFieldRenderable3D(val source: ScalarField, override val radiuses: RadiusesProperty[_1D], override val opacity: OpacityProperty) extends Renderable with HasOpacity with HasRadiuses[_1D]

  object DefaultVisualizationStrategy extends VisualizationStrategy[ScalarField] {
    override def renderablesFor2D(targetObject: ScalarField): scala.Seq[Renderable] = Seq()

    override def renderablesFor3D(t: ScalarField): scala.Seq[Renderable] = Seq(new ScalarFieldRenderable3D(t, t.radiuses, t.opacity))
  }
}

trait ScalarField extends ThreeDRepresentation[ScalarField] with Landmarkable with HasOpacity with HasRadiuses[_1D] {

  override val opacity: OpacityProperty = new OpacityProperty(None)
  override val radiuses: RadiusesProperty[_1D] = new RadiusesProperty(None)

  override def visualizationStrategy: VisualizationStrategy[ScalarField] = ScalarField.DefaultVisualizationStrategy

  def peer: DiscreteScalarField[_3D, Float]
}
