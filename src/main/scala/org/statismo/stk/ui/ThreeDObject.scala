package org.statismo.stk.ui

trait ThreeDObject extends SceneTreeObject {
  lazy val representations: ThreeDRepresentations = new ThreeDRepresentations(this)

  def landmarks: VisualizableLandmarks

  override def children = {
    val lm: Seq[SceneTreeObject] = Seq(landmarks)
    val rep: Seq[SceneTreeObject] = representations.children
    Seq(lm, rep).flatten
  }
}