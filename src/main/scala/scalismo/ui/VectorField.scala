package scalismo.ui

import scalismo.common.DiscreteVectorField
import scalismo.geometry._3D
import scalismo.ui.visualization._
import scalismo.ui.visualization.props._

import scala.collection.immutable.Seq

object VectorField {

  class VectorFieldRenderable3D(val source: VectorField, override val opacity: OpacityProperty) extends Renderable with HasOpacity

  object DefaultVisualizationStrategy extends VisualizationStrategy[VectorField] {
    override def renderablesFor2D(targetObject: VectorField): scala.Seq[Renderable] = Seq()

    override def renderablesFor3D(t: VectorField): scala.Seq[Renderable] = Seq(new VectorFieldRenderable3D(t, t.opacity))
  }

}

trait VectorField extends ThreeDRepresentation[VectorField] with HasOpacity {

  override val opacity: OpacityProperty = new OpacityProperty(None)

  override def visualizationStrategy: VisualizationStrategy[VectorField] = VectorField.DefaultVisualizationStrategy

  def peer: DiscreteVectorField[_3D, _3D]
}

object StaticVectorField {
  def createFromPeer(peer: DiscreteVectorField[_3D, _3D], parent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit scene: Scene): StaticVectorField = {
    new StaticVectorField(peer, parent, name)
  }
}

class StaticVectorField private[StaticVectorField] (override val peer: DiscreteVectorField[_3D, _3D], initialParent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit override val scene: Scene) extends VectorField {

  name_=(name.getOrElse(Nameable.NoName))

  override lazy val parent: StaticThreeDObject = initialParent.getOrElse(new StaticThreeDObject(Some(scene.staticObjects), name))

  parent.representations.add(this)
}
