package org.statismo.stk.ui

import org.statismo.stk.ui.visualization.{VisualizableSceneTreeObject, Visualizable}

class ThreeDRepresentations(override val publisher: ThreeDObject) extends SceneTreeObjectContainer[ThreeDRepresentation[_]] with RemoveableChildren {
}

trait ThreeDRepresentation[C <: VisualizableSceneTreeObject[C]] extends SceneTreeObject with Removeable with VisualizableSceneTreeObject[C] {
  def threeDObject = parent.asInstanceOf[ThreeDObject]
}