package scalismo.ui

import scalismo.common.Scalar
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

object StaticScalarMeshField {
  def createFromPeer[A: Scalar](peer: scalismo.mesh.ScalarMeshField[A], parent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit scene: Scene): StaticScalarMeshField = {
    val scalar = implicitly[Scalar[A]]
    val floatField = peer.map(s => scalar.toFloat(s))
    new StaticScalarMeshField(floatField, parent, name)
  }
}

class StaticScalarMeshField private[StaticScalarMeshField] (override val peer: scalismo.mesh.ScalarMeshField[Float],
    initialParent: Option[StaticThreeDObject] = None,
    name: Option[String] = None)(implicit override val scene: Scene) extends ScalarMeshField {

  name_=(name.getOrElse(Nameable.NoName))

  override lazy val parent: StaticThreeDObject = initialParent.getOrElse(new StaticThreeDObject(Some(scene.staticObjects), name))

  parent.representations.add(this)
}
