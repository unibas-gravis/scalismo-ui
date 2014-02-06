package org.statismo.stk.ui

class StaticThreeDObjects(implicit override val scene: Scene) extends SceneTreeObjectContainer[ThreeDObject] {
  name = "Static Objects"
  override lazy val isNameUserModifiable = false
  override lazy val parent = scene
}

class StaticThreeDObject(initialParent: Option[SceneTreeObjectContainer[ThreeDObject]] = None, initialName: String = Nameable.DefaultName)(implicit override val scene: Scene) extends ThreeDObject {
  override lazy val parent = initialParent.getOrElse(scene.statics)
  override lazy val landmarks = new StaticLandmarks(this)
  name = initialName
  parent.add(this)
}