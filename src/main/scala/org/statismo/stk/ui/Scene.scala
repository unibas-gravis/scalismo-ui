package org.statismo.stk.ui

import scala.collection.immutable.List
import scala.swing.event.Event
import scala.util.Try
import org.statismo.stk.ui.visualization.{Visualizable, Visualizations}
import org.statismo.stk.core.geometry.Point3D

object Scene {
  case class TreeTopologyChanged(scene: Scene) extends Event
  case class PerspectiveChanged(scene: Scene) extends Event
  case class VisibilityChanged(scene: Scene) extends Event
  case class BoundingBoxChanged(scene: Scene) extends Event
  case class SlicingPositionChanged(scene: Scene) extends Event

}

class Scene extends SceneTreeObject {
  deafTo(this)
  org.statismo.stk.core.initialize()

  name = "Scene"
  override lazy val isNameUserModifiable = false

  override implicit lazy val parent = this

  private var _perspective: Perspective = {
    val p = Perspective.defaultPerspective(this)
    // initial setup
    onViewportsChanged(p.viewports)
    p
  }

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
    case Viewport.Destroyed(v) => deafTo(v)
    case Viewport.BoundingBoxChanged(v) => slicingPosition.updateBoundingBox()
    case SceneTreeObject.VisibilityChanged(s) =>
      publish(Scene.VisibilityChanged(this))
    case SceneTreeObject.ChildrenChanged(s) =>
      publish(Scene.TreeTopologyChanged(this))
    case m@Nameable.NameChanged(s) =>
      publish(m)
  }


  override def onViewportsChanged(viewports: Seq[Viewport]) = {
    viewports.foreach(listenTo(_))
    super.onViewportsChanged(viewports)
  }

  lazy val visualizations: Visualizations = new Visualizations

  lazy val slicingPosition: SlicingPosition = new SlicingPosition

  class SlicingPosition {
    private var _point = new Point3D(0,0,0)
    private def point = _point
    private def point_=(np: Point3D) = this.synchronized {
      if (_point != np) {
        _point = np
      }
      scene.publish(Scene.SlicingPositionChanged(scene))
    }

    def x = this.synchronized{_point.x}
    def y = this.synchronized{_point.y}
    def z = this.synchronized{_point.z}

    def x_=(nv: Float) = this.synchronized {
      val sv = Math.min(Math.max(boundingBox.xMin, nv), boundingBox.xMax)
      if (x != sv) {
        point_=(new Point3D(sv,y, z))
      }
    }

    def y_=(nv: Float) = this.synchronized {
      val sv = Math.min(Math.max(boundingBox.yMin, nv), boundingBox.yMax)
      if (y != sv) {
        point = new Point3D(x, sv, z)
      }
    }

    def z_=(nv: Float) = this.synchronized {
      val sv = Math.min(Math.max(boundingBox.zMin, nv), boundingBox.zMax)
      if (z != sv) {
        point = new Point3D(x ,y, sv)
      }
    }

    private var _boundingBox = BoundingBox.Zero
    def boundingBox = this.synchronized(_boundingBox)
    private[Scene] def boundingBox_=(nb: BoundingBox) = this.synchronized {
      if (boundingBox != nb) {
        _boundingBox = nb
        scene.publish(Scene.BoundingBoxChanged(scene))
      }
    }

    private[Scene] def updateBoundingBox() = {
      boundingBox = scene.viewports.foldLeft(BoundingBox.Zero)({case (bb, vp) => bb.union(vp.currentBoundingBox)})
    }
  }
}

class AuxiliaryObjects()(implicit override val scene: Scene) extends StandaloneSceneTreeObjectContainer[Visualizable[_]] {
  name = "Auxiliary Objects"
  override lazy val isNameUserModifiable = false
  override lazy val parent = scene
}
