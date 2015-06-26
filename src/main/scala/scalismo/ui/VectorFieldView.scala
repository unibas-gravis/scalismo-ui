package scalismo.ui

import scalismo.common.DiscreteVectorField
import scalismo.geometry._3D
import scalismo.ui.visualization._
import scalismo.ui.visualization.props._

import scala.collection.immutable.Seq

object VectorFieldView {

  class VectorFieldRenderable3D(val source: VectorFieldView, override val opacity: OpacityProperty) extends Renderable with HasOpacity

  object DefaultVisualizationStrategy extends VisualizationStrategy[VectorFieldView] {
    override def renderablesFor2D(targetObject: VectorFieldView): scala.Seq[Renderable] = Seq()

    override def renderablesFor3D(t: VectorFieldView): scala.Seq[Renderable] = Seq(new VectorFieldRenderable3D(t, t.opacity))
  }

  def createFromUnderlying(peer: DiscreteVectorField[_3D, _3D], parent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit scene: Scene): StaticVectorFieldView = {
    new StaticVectorFieldView(peer, parent, name)
  }

}

trait VectorFieldView extends UIView[DiscreteVectorField[_3D, _3D]] with ThreeDRepresentation[VectorFieldView] with HasOpacity {

  override val opacity: OpacityProperty = new OpacityProperty(None)

  override def visualizationStrategy: VisualizationStrategy[VectorFieldView] = VectorFieldView.DefaultVisualizationStrategy

}

class StaticVectorFieldView private[ui] (override val underlying: DiscreteVectorField[_3D, _3D], initialParent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit override val scene: Scene) extends VectorFieldView {

  name_=(name.getOrElse(Nameable.NoName))

  override lazy val parent: StaticThreeDObject = initialParent.getOrElse(new StaticThreeDObject(Some(scene.staticObjects), name))

  parent.representations.add(this)
}
