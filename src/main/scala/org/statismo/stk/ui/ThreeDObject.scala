package org.statismo.stk.ui

trait ThreeDObject extends SceneTreeObject {
	lazy val representations: ThreeDRepresentations = new ThreeDRepresentations(this)
	def landmarks: DisplayableLandmarks
	override def children = Seq(representations, landmarks)
}