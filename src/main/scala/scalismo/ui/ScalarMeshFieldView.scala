package scalismo.ui

import scalismo.common.Scalar
import scalismo.mesh.ScalarMeshField
import scalismo.ui.MeshView.MeshRenderable
import scalismo.ui.visualization._
import scalismo.ui.visualization.props._

import scala.collection.immutable.Seq

object ScalarMeshFieldView {

  class ScalarMeshFieldRenderable(override val source: ScalarMeshFieldView, override val scalarRange: ScalarRangeProperty, opacity: OpacityProperty, lineWidth: LineWidthProperty) extends MeshRenderable[ScalarMeshFieldView](source, opacity, lineWidth) with HasScalarRange

  //class ScalarMeshFieldRenderable3D(val source: ScalarMeshFieldView, override val opacity: OpacityProperty, override val scalarRange: ScalarRangeProperty) extends Renderable with HasOpacity with HasScalarRange

  object DefaultVisualizationStrategy extends VisualizationStrategy[ScalarMeshFieldView] {
    override def renderablesFor2D(targetObject: ScalarMeshFieldView): scala.Seq[Renderable] = renderablesFor3D(targetObject)

    override def renderablesFor3D(t: ScalarMeshFieldView): scala.Seq[Renderable] = Seq(new ScalarMeshFieldRenderable(t, t.scalarRange, t.opacity, t.lineWidth))
  }

  def createFromSource[A: Scalar](source: scalismo.mesh.ScalarMeshField[A], parent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit scene: Scene): StaticScalarMeshFieldView = {
    val scalar = implicitly[Scalar[A]]
    val floatField = source.map(s => scalar.toFloat(s))
    new StaticScalarMeshFieldView(floatField, parent, name)
  }
}

trait ScalarMeshFieldView extends UIView[ScalarMeshField[Float]] with ThreeDRepresentation[ScalarMeshFieldView] with HasOpacity with HasLineWidth with HasScalarRange {

  override val opacity: OpacityProperty = new OpacityProperty(None)
  override val lineWidth: LineWidthProperty = new LineWidthProperty(None)
  override val scalarRange: ScalarRangeProperty = new ScalarRangeProperty({
    val (min, max) = (source.values.min, source.values.max)
    Some(ScalarRange(min, max, min, max))
  })

  override def visualizationStrategy: VisualizationStrategy[ScalarMeshFieldView] = ScalarMeshFieldView.DefaultVisualizationStrategy
}

class StaticScalarMeshFieldView private[ui] (override val source: scalismo.mesh.ScalarMeshField[Float],
    initialParent: Option[StaticThreeDObject] = None,
    name: Option[String] = None)(implicit override val scene: Scene) extends ScalarMeshFieldView {

  name_=(name.getOrElse(Nameable.NoName))

  override lazy val parent: StaticThreeDObject = initialParent.getOrElse(new StaticThreeDObject(Some(scene.staticObjects), name))

  parent.representations.add(this)
}
