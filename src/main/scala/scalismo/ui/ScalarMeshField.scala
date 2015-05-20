package scalismo.ui

import scalismo.geometry._3D
import scalismo.ui.visualization._
import scalismo.ui.visualization.props._

import scala.collection.immutable.Seq

object ScalarMeshField extends SimpleVisualizationFactory[ScalarMeshField] {

  visualizations += Tuple2(Viewport.ThreeDViewportClassName, Seq(new Visualization3D(None)))
  visualizations += Tuple2(Viewport.TwoDViewportClassName, Seq(new NullVisualization[ScalarMeshField]))

  class Visualization3D(from: Option[Visualization3D]) extends Visualization[ScalarMeshField] with HasColorAndOpacity {
    override val color: ColorProperty = if (from.isDefined) from.get.color.derive() else new ColorProperty(None)
    override val opacity: OpacityProperty = if (from.isDefined) from.get.opacity.derive() else new OpacityProperty(None)

    protected def createDerived() = new Visualization3D(Some(this))

    protected def instantiateRenderables(source: ScalarMeshField) = {
      Seq(new ScalarMeshFieldRenderable3D(source, color, opacity))
    }

    override val description: String = "Scalars"
  }

  class ScalarMeshFieldRenderable3D(val source: ScalarMeshField, override val color: ColorProperty, override val opacity: OpacityProperty) extends Renderable with HasColorAndOpacity

}

trait ScalarMeshField extends ThreeDRepresentation[ScalarMeshField] {
  def peer: scalismo.mesh.ScalarMeshField[Float]

  protected[ui] override def visualizationProvider: VisualizationProvider[ScalarMeshField] = ScalarMeshField
}