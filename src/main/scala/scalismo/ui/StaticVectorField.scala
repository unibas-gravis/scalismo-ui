package scalismo.ui

import scalismo.common.DiscreteVectorField
import scalismo.geometry._3D

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