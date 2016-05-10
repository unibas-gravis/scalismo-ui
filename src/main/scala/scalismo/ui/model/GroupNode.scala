package scalismo.ui.model

import scalismo.statisticalmodel.StatisticalMeshModel
import scalismo.ui.event.ScalismoPublisher
import scalismo.ui.model.Scene.event.SceneChanged
import scalismo.ui.model.capabilities.{ Removeable, Renameable }

class GroupsNode(override val parent: Scene) extends SceneNodeCollection[GroupNode] {
  override val name = "Groups"

  def add(name: String, ghost: Boolean = false): GroupNode = {
    val node = new GroupNode(this, name, ghost)
    add(node)
    node
  }

  // the groups node is always collapsed in the view.
  override def isViewCollapsed: Boolean = true
}

class GroupNode(override val parent: GroupsNode, initialName: String,  private var _isGhost: Boolean) extends SceneNode with Renameable with Removeable with ScalismoPublisher {
  name = initialName


  def isGhost_=(b : Boolean): Unit = {
    _isGhost = b
    scene.publishEvent(SceneChanged(scene))
  }
  def isGhost = _isGhost

  val transformations = new TransformationsNode(this)
  val landmarks = new LandmarksNode(this)
  val triangleMeshes = new TriangleMeshesNode(this)
  val scalarMeshFields = new ScalarMeshFieldsNode(this)
  val vectorFields = new VectorFieldsNode(this)
  val pointClouds = new PointCloudsNode(this)
  val images = new ImagesNode(this)
  val scalarFields = new ScalarFieldsNode(this)

  override val children: List[SceneNode] = List(transformations, landmarks, triangleMeshes, scalarMeshFields, pointClouds, images, scalarFields, vectorFields)

  // this is a convenience method to add a statistical model as a (gp, mesh) combination.
  def addStatisticalMeshModel(model: StatisticalMeshModel, initialName: String): Unit = {
    triangleMeshes.add(model.referenceMesh, initialName)
    transformations.add(DiscreteLowRankGpPointTransformation(model.gp), initialName)

  }


  override def remove(): Unit = parent.remove(this)
}

