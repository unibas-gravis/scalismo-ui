package org.statismo.stk.ui

import scala.collection.immutable.List
import scala.swing.event.Event
import scala.util.Try

object Scene {
  case class TreeTopologyChanged(scene: Scene) extends Event
  case class PerspectiveChanged(scene: Scene) extends Event
  case class VisibilityChanged(scene: Scene) extends Event
}

class Scene extends SceneTreeObject {
  deafTo(this)
  org.statismo.stk.core.initialize()

  name = "Scene"
  override lazy val isNameUserModifiable = false

  override implicit lazy val parent = this

  private var _perspective: Perspective = Perspective.defaultPerspective(this)

  def perspective = _perspective
  def perspective_=(newPerspective: Perspective) = {
    if (newPerspective ne _perspective) {
      _perspective.viewports foreach (_.destroy())
      _perspective = newPerspective
      onViewportsChanged(newPerspective.viewports)
      publish(Scene.PerspectiveChanged(this))
    }
  }

  def viewports = perspective.viewports

  val shapeModels = new ShapeModels
  val staticObjects = new StaticThreeDObjects
  val auxiliaryObjects = new AuxiliaryObjects

  override val children = List(shapeModels, staticObjects) //, auxiliaries)

  def tryLoad(filename: String, factories: Seq[SceneTreeObjectFactory[SceneTreeObject]] = SceneTreeObjectFactory.DefaultFactories): Try[SceneTreeObject] = {
    SceneTreeObjectFactory.load(filename, factories)
  }

  reactions += {
    case SceneTreeObject.VisibilityChanged(s) => {
      publish(Scene.VisibilityChanged(this))
    }
    case SceneTreeObject.ChildrenChanged(s) => {
      publish(Scene.TreeTopologyChanged(this))
    }
    case m @ Nameable.NameChanged(s) => {
      publish(m)
    }
  }

}

class AuxiliaryObjects()(implicit override val scene: Scene) extends StandaloneSceneTreeObjectContainer[Displayable] {
  name = "Auxiliary Objects"
  override lazy val isNameUserModifiable = false
  override lazy val parent = scene
}
