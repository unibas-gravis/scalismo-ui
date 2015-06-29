package scalismo.ui

import scalismo.common.Scalar
import scalismo.mesh.ScalarMeshField
import scalismo.ui.visualization._
import scalismo.ui.visualization.props._

import scala.collection.immutable.Seq

object ScalarMeshFieldView {

  class ScalarMeshFieldRenderable3D(val source: ScalarMeshFieldView, override val color: ColorProperty, override val opacity: OpacityProperty) extends Renderable with HasColorAndOpacity

  object DefaultVisualizationStrategy extends VisualizationStrategy[ScalarMeshFieldView] {
    override def renderablesFor2D(targetObject: ScalarMeshFieldView): scala.Seq[Renderable] = Seq()

    override def renderablesFor3D(t: ScalarMeshFieldView): scala.Seq[Renderable] = Seq(new ScalarMeshFieldRenderable3D(t, t.color, t.opacity))
  }

  def createFromSource[A: Scalar](source: scalismo.mesh.ScalarMeshField[A], parent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit scene: Scene): StaticScalarMeshFieldView = {
    val scalar = implicitly[Scalar[A]]
    val floatField = source.map(s => scalar.toFloat(s))
    new StaticScalarMeshFieldView(floatField, parent, name)
  }
}

trait ScalarMeshFieldView extends UIView[ScalarMeshField[Float]] with ThreeDRepresentation[ScalarMeshFieldView] with HasColorAndOpacity {

  override val color: ColorProperty = new ColorProperty(None)
  override val opacity: OpacityProperty = new OpacityProperty(None)

  override def visualizationStrategy: VisualizationStrategy[ScalarMeshFieldView] = ScalarMeshFieldView.DefaultVisualizationStrategy
}

class StaticScalarMeshFieldView private[ui] (override val source: scalismo.mesh.ScalarMeshField[Float],
    initialParent: Option[StaticThreeDObject] = None,
    name: Option[String] = None)(implicit override val scene: Scene) extends ScalarMeshFieldView {

  name_=(name.getOrElse(Nameable.NoName))

  override lazy val parent: StaticThreeDObject = initialParent.getOrElse(new StaticThreeDObject(Some(scene.staticObjects), name))

  parent.representations.add(this)
}
