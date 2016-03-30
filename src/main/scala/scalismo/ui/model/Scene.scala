package scalismo.ui.model

import scalismo.ui.control.SceneControl
import scalismo.ui.event.Event
import scalismo.ui.model.Scene.event.SceneChanged
import scalismo.ui.model.SceneNode.event.ChildrenChanged
import scalismo.ui.view.ScalismoFrame

object Scene {

  object event {

    case class SceneChanged(scene: Scene) extends Event

  }

}

class Scene extends SceneNode {
  override val name: String = "Scene"

  override lazy val scene: Scene = this

  override val parent: SceneNode = {
    /* A Scene is the only SceneNode without a parent, and should be handled accordingly.
     * If some algorithm carelessly tries to access the scene's parent, the NPE that will
     * get thrown is actually a feature ("you are doing something wrong!"), not a bug.
     */
    null
  }

  val groups = new GroupsNode(this)

  override val children: List[SceneNode] = List(groups)

  reactions += {
    case ChildrenChanged(node) => publishEvent(SceneChanged(this))
  }

  def control(implicit frame: ScalismoFrame): SceneControl = {
    val result = frame.sceneControl
    require(result.scene == this)
    result
  }

}
