package scalismo.ui

import scalismo.common.DiscreteVectorField
import scalismo.geometry.{ Point, _1D, _3D }
import scalismo.ui.visualization._
import scalismo.ui.visualization.props._

import scala.collection.immutable
import scala.collection.immutable.Seq

object VectorField extends SimpleVisualizationFactory[VectorField] {

  visualizations += Tuple2(Viewport.ThreeDViewportClassName, Seq(new Visualization3D(None)))
  visualizations += Tuple2(Viewport.TwoDViewportClassName, Seq(new NullVisualization[VectorField]))

  class Visualization3D(from: Option[Visualization3D]) extends Visualization[VectorField] with HasColorAndOpacity {
    override val color: ColorProperty = if (from.isDefined) from.get.color.derive() else new ColorProperty(None)
    override val opacity: OpacityProperty = if (from.isDefined) from.get.opacity.derive() else new OpacityProperty(None)

    protected def createDerived() = new Visualization3D(Some(this))

    protected def instantiateRenderables(source: VectorField) = {
      Seq(new VectorFieldRenderable3D(source, color, opacity))
    }

    override val description: String = "Vectors"
  }

  class VectorFieldRenderable3D(val source: VectorField, override val color: ColorProperty, override val opacity: OpacityProperty) extends Renderable with HasColorAndOpacity

}

trait VectorField extends ThreeDRepresentation[VectorField] {
  def peer: DiscreteVectorField[_3D, _3D]

  protected[ui] override def visualizationProvider: VisualizationProvider[VectorField] = VectorField
}