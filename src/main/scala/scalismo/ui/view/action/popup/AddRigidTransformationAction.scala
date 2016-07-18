package scalismo.ui.view.action.popup

import scalismo.ui.model.{ GenericTransformationsNode, GenericTransformationsNode$, PointTransformation, SceneNode }
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.view.ScalismoFrame

object AddRigidTransformationAction extends PopupAction.Factory {
  override def apply(context: List[SceneNode])(implicit frame: ScalismoFrame): List[PopupAction] = {
    singleMatch[GenericTransformationsNode](context).map(n => new AddRigidTransformationAction(n)).toList
  }
}

class AddRigidTransformationAction(node: GenericTransformationsNode)(implicit val frame: ScalismoFrame) extends PopupAction("Add rigid transformation", BundledIcon.Transformation) with GroupDelegatingAction {
  override def apply(): Unit = {
    node.add(PointTransformation.RigidIdentity, "rigid")
  }
}
