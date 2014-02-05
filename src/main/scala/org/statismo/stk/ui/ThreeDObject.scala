package org.statismo.stk.ui

trait ThreeDObject extends SceneTreeObject {
	val representations: ThreeDRepresentations = new ThreeDRepresentations(this)
	override val children = Seq(representations)
}