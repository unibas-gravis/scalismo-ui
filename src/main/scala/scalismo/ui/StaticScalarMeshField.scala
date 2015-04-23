package scalismo.ui


import scalismo.common.{DiscreteScalarField, Scalar}

import scalismo.geometry._3D

object StaticScalarMeshField {
  def createFromPeer[A : Scalar](peer: scalismo.mesh.ScalarMeshField[A], parent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit scene: Scene): StaticScalarMeshField = {
    val scalar = implicitly[Scalar[A]]
    val floatField = peer.map(s => scalar.toFloat(s))
    new StaticScalarMeshField(floatField, parent, name)
  }
}

class StaticScalarMeshField private[StaticScalarMeshField](override val peer: DiscreteScalarField[_3D, Float],
                                                               initialParent: Option[StaticThreeDObject] = None,
                                                               name: Option[String] = None)
                                                              (implicit override val scene: Scene) extends ScalarMeshField {

  name_=(name.getOrElse(Nameable.NoName))

  override lazy val parent: StaticThreeDObject = initialParent.getOrElse(new StaticThreeDObject(Some(scene.staticObjects), name))

  parent.representations.add(this)
}