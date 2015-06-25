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
}

class StaticThreeDObject(initialParent: Option[StandaloneSceneTreeObjectContainer[StaticThreeDObject]] = None, name: Option[String] = None)(implicit override val scene: Scene) extends ThreeDObject with Removeable {
  override lazy val parent: StandaloneSceneTreeObjectContainer[StaticThreeDObject] = initialParent.getOrElse(scene.staticObjects)
  override lazy val landmarks: StaticLandmarks = new StaticLandmarks(this)
  name_=(name.getOrElse(Nameable.NoName))
  parent.add(this)
}
