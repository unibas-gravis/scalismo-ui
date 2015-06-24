package scalismo.ui

import scalismo.common.Scalar

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
