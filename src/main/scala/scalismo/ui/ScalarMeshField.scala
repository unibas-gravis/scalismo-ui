package scalismo.ui

import scalismo.ui.visualization._
import scalismo.ui.visualization.props._

import scala.collection.immutable.Seq

object ScalarMeshField {

  class ScalarMeshFieldRenderable3D(val source: ScalarMeshField, override val color: ColorProperty, override val opacity: OpacityProperty) extends Renderable with HasColorAndOpacity

  object DefaultVisualizationStrategy extends VisualizationStrategy[ScalarMeshField] {
    override def renderablesFor2D(targetObject: ScalarMeshField): scala.Seq[Renderable] = Seq()

    override def renderablesFor3D(t: ScalarMeshField): scala.Seq[Renderable] = Seq(new ScalarMeshFieldRenderable3D(t, t.color, t.opacity))
  }
}

trait ScalarMeshField extends ThreeDRepresentation[ScalarMeshField] with HasColorAndOpacity {

  override val color: ColorProperty = new ColorProperty(None)
  override val opacity: OpacityProperty = new OpacityProperty(None)

  override def visualizationStrategy: VisualizationStrategy[ScalarMeshField] = ScalarMeshField.DefaultVisualizationStrategy

  def peer: scalismo.mesh.ScalarMeshField[Float]
}
