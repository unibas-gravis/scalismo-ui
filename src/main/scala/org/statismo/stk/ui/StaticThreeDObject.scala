package org.statismo.stk.ui

class StaticThreeDObjects(implicit override val scene: Scene) extends StandaloneSceneTreeObjectContainer[StaticThreeDObject] with RemoveableChildren {
  name = "Static Objects"
  override lazy val isNameUserModifiable = false
  override lazy val parent = scene
}

class StaticThreeDObject(initialParent: Option[StandaloneSceneTreeObjectContainer[StaticThreeDObject]] = None, name: Option[String] = None)(implicit override val scene: Scene) extends ThreeDObject with Removeable {
  override lazy val parent = initialParent.getOrElse(scene.staticObjects)
  override lazy val landmarks = new StaticLandmarks(this)
  name_=(name.getOrElse(Nameable.NoName))
  parent.add(this)
}