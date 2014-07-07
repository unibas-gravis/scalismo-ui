package org.statismo.stk.ui

import org.statismo.stk.ui.visualization.VisualizableSceneTreeObject

class ThreeDRepresentations(override val publisher: ThreeDObject) extends SceneTreeObjectContainer[ThreeDRepresentation[_]] with RemoveableChildren {
}

trait ThreeDRepresentation[C <: VisualizableSceneTreeObject[C]] extends SceneTreeObject with Removeable with VisualizableSceneTreeObject[C] {
  protected def threeDObject = parent.asInstanceOf[ThreeDObject]
}