package scalismo.ui

import java.io.File

import scalismo.common.Scalar
import scalismo.geometry._3D
import scalismo.image.DiscreteScalarImage
import scalismo.mesh.TriangleMesh

import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag
import scala.util.Try

class StaticThreeDObjects(implicit override val scene: Scene) extends StandaloneSceneTreeObjectContainer[StaticThreeDObject] with RemoveableChildren {
  name = "Static Objects"
  protected[ui] override lazy val isNameUserModifiable = false
  override lazy val parent = scene

  def createFromMeshFile(file: File): Try[StaticThreeDObject] = StaticMesh.createFromFile(file, None, file.getName).map { mesh => mesh.parent }

  def createFromMeshPeer(peer: TriangleMesh, name: Option[String] = None): StaticThreeDObject = StaticMesh.createFromPeer(peer, None, name).parent

  def createFromImageFile(file: File): Try[StaticThreeDObject] = StaticImage3D.createFromFile(file, None, file.getName).map { img => img.parent }

  def createFromImagePeer[S: Scalar: ClassTag: TypeTag](peer: DiscreteScalarImage[_3D, S], name: Option[String] = None): StaticThreeDObject = StaticImage3D.createFromPeer(peer, None, name).parent
}

class StaticThreeDObject(initialParent: Option[StandaloneSceneTreeObjectContainer[StaticThreeDObject]] = None, name: Option[String] = None)(implicit override val scene: Scene) extends ThreeDObject with Removeable {
  override lazy val parent: StandaloneSceneTreeObjectContainer[StaticThreeDObject] = initialParent.getOrElse(scene.staticObjects)
  override lazy val landmarks: StaticLandmarks = new StaticLandmarks(this)
  name_=(name.getOrElse(Nameable.NoName))
  parent.add(this)
}
