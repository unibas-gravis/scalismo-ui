package scalismo.ui

import scalismo.geometry.{ Point, _3D }

import scala.collection.immutable

object StaticPointCloud {
  def createFromPeer(peer: immutable.IndexedSeq[Point[_3D]], parent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit scene: Scene): StaticPointCloud = {
    new StaticPointCloud(peer, parent, name)
  }
}

class StaticPointCloud private[StaticPointCloud] (override val peer: immutable.IndexedSeq[Point[_3D]], initialParent: Option[StaticThreeDObject] = None, name: Option[String] = None)(implicit override val scene: Scene) extends PointCloud {

  name_=(name.getOrElse(Nameable.NoName))

  override lazy val parent: StaticThreeDObject = initialParent.getOrElse(new StaticThreeDObject(Some(scene.staticObjects), name))

  override def addLandmarkAt(point: Point[_3D], nameOpt: Option[String]) = {
    val landmarks = parent.landmarks
    landmarks.addAt(point, nameOpt, Uncertainty.defaultUncertainty3D())
  }

  parent.representations.add(this)
}