package scalismo.ui

import scalismo.common.{ DiscreteScalarField, Scalar }
import scalismo.geometry.{ Point, _1D, _3D }
import scalismo.ui.visualization._
import scalismo.ui.visualization.props._

import scala.collection.immutable.Seq

object ScalarFieldView {

  class ScalarFieldRenderable(val source: ScalarFieldView, override val radiuses: RadiusesProperty[_1D], override val opacity: OpacityProperty, override val scalarRange: ScalarRangeProperty) extends Renderable with HasOpacity with HasScalarRange with HasRadiuses[_1D]

  object DefaultVisualizationStrategy extends VisualizationStrategy[ScalarFieldView] {
    override def renderablesFor2D(t: ScalarFieldView): scala.Seq[Renderable] = renderablesFor3D(t)

    override def renderablesFor3D(t: ScalarFieldView): scala.Seq[Renderable] = Seq(new ScalarFieldRenderable(t, t.radiuses, t.opacity, t.scalarRange))
  }

  def createFromSource[A: Scalar](source: scalismo.common.DiscreteScalarField[_3D, A], parent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit scene: Scene): StaticScalarFieldView = {
    val scalar = implicitly[Scalar[A]]
    val floatField = source.map(s => scalar.toFloat(s))
    new StaticScalarFieldView(floatField, parent, name)
  }

}

trait ScalarFieldView extends UIView[DiscreteScalarField[_3D, Float]] with ThreeDRepresentation[ScalarFieldView] with Landmarkable with HasOpacity with HasScalarRange with HasRadiuses[_1D] {

  override val opacity: OpacityProperty = new OpacityProperty(None)
  override val scalarRange: ScalarRangeProperty = new ScalarRangeProperty({
    val (min, max) = (source.values.min, source.values.max)
    Some(ScalarRange(min, max, min, max))
  })
  override val radiuses: RadiusesProperty[_1D] = new RadiusesProperty(None)

  override def visualizationStrategy: VisualizationStrategy[ScalarFieldView] = ScalarFieldView.DefaultVisualizationStrategy

}

class StaticScalarFieldView private[ui] (override val source: DiscreteScalarField[_3D, Float],
    initialParent: Option[StaticThreeDObject] = None,
    name: Option[String] = None)(implicit override val scene: Scene) extends ScalarFieldView {

  name_=(name.getOrElse(Nameable.NoName))

  override lazy val parent: StaticThreeDObject = initialParent.getOrElse(new StaticThreeDObject(Some(scene.staticObjects), name))

  parent.representations.add(this)

  override def addLandmarkAt(point: Point[_3D], nameOpt: Option[String]) = {
    val landmarks = parent.landmarks
    landmarks.addAt(point, nameOpt, Uncertainty.defaultUncertainty3D())
  }

}