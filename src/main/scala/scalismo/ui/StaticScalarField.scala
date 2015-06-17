package scalismo.ui

import scalismo.common.{ DiscreteScalarField, Scalar }
import scalismo.geometry.{ Point, _3D }

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