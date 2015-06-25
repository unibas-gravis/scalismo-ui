package scalismo.ui

import scalismo.common.{ Scalar, DiscreteScalarField }
import scalismo.geometry.{ Point, _1D, _3D }
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

object StaticScalarField {
  def createFromPeer[A: Scalar](peer: scalismo.common.DiscreteScalarField[_3D, A], parent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit scene: Scene): StaticScalarField = {
    val scalar = implicitly[Scalar[A]]
    val floatField = peer.map(s => scalar.toFloat(s))
    new StaticScalarField(floatField, parent, name)
  }
}

class StaticScalarField private[StaticScalarField] (override val peer: DiscreteScalarField[_3D, Float],
    initialParent: Option[StaticThreeDObject] = None,
    name: Option[String] = None)(implicit override val scene: Scene) extends ScalarField {

  name_=(name.getOrElse(Nameable.NoName))

  override lazy val parent: StaticThreeDObject = initialParent.getOrElse(new StaticThreeDObject(Some(scene.staticObjects), name))

  parent.representations.add(this)

  override def addLandmarkAt(point: Point[_3D], nameOpt: Option[String]) = {
    val landmarks = parent.landmarks
    landmarks.addAt(point, nameOpt, Uncertainty.defaultUncertainty3D())
  }

}