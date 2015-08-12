package scalismo.ui

import scalismo.geometry.{ Point, _1D, _3D }
import scalismo.ui.visualization._
import scalismo.ui.visualization.props._

import scala.collection.immutable
import scala.collection.immutable.Seq

object PointCloudView {

  class PointCloudRenderable3D(val source: PointCloudView, override val color: ColorProperty, override val opacity: OpacityProperty, override val radiuses: RadiusesProperty[_1D]) extends Renderable with HasColorAndOpacity with HasRadiuses[_1D]

  object DefaultVisualizationStrategy extends VisualizationStrategy[PointCloudView] {
    override def renderablesFor2D(targetObject: PointCloudView): scala.Seq[Renderable] = Seq()

    override def renderablesFor3D(t: PointCloudView): scala.Seq[Renderable] = Seq(new PointCloudRenderable3D(t, t.color, t.opacity, t.radiuses))
  }

  def createFromSource(source: immutable.IndexedSeq[Point[_3D]], parent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit scene: Scene): StaticPointCloudView = {
    new StaticPointCloudView(source, parent, name)
  }
}

trait PointCloudView extends UIView[immutable.IndexedSeq[Point[_3D]]] with ThreeDRepresentation[PointCloudView] with Landmarkable with HasColorAndOpacity with HasRadiuses[_1D] {

  override val color: ColorProperty = new ColorProperty(None)
  override val opacity: OpacityProperty = new OpacityProperty(None)
  override val radiuses: RadiusesProperty[_1D] = new RadiusesProperty(None)

  override def visualizationStrategy: VisualizationStrategy[PointCloudView] = PointCloudView.DefaultVisualizationStrategy

}

class StaticPointCloudView private[ui] (override val source: immutable.IndexedSeq[Point[_3D]], initialParent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit override val scene: Scene) extends PointCloudView {

  name_=(name.getOrElse(Nameable.NoName))

  override lazy val parent: StaticThreeDObject = initialParent.getOrElse(new StaticThreeDObject(Some(scene.staticObjects), name))

  override def addLandmarkAt(point: Point[_3D], nameOpt: Option[String]) = {
    val landmarks = parent.landmarks
    landmarks.addAt(point, nameOpt, Uncertainty.defaultUncertainty3D())
  }

  parent.representations.add(this)
}
