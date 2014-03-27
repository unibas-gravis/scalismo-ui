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

  object SlicingPosition {
    case class BoundingBoxChanged(slicingPosition: SlicingPosition) extends Event
    case class PointChanged(slicingPosition: SlicingPosition) extends Event
    case class PrecisionChanged(slicingPosition: SlicingPosition) extends Event

    object Precision extends Enumeration {
      import scala.language.implicitConversions
      protected case class Val(name: String, format: Float => String, toInteger: Float => Int, fromInteger: Int => Float) extends super.Val(nextId, name) {
      }
      implicit def valueToPrecisionVal(x: Value) = x.asInstanceOf[Val]

      val MmWhole = new Val("1 mm", {value => f"$value%1.0f"}, {f => Math.round(f)}, {i => i.toFloat} )
      val MmTenth = new Val("1/10 mm", {value => f"$value%1.1f"}, {f => Math.round(f * 10)}, {i => i.toFloat / 10f})
      val MmHundredth = new Val("1/100 mm", {value => f"$value%1.2f"}, {f => Math.round(f * 100)}, {i => i.toFloat  / 100f})
    }
  }

  class SlicingPosition(val scene: Scene) {
    import Scene.SlicingPosition.Precision
    private var _point = new Point3D(0,0,0)
    def point = this.synchronized {_point}
    private def point_=(np: Point3D) = this.synchronized {
      if (_point != np) {
        _point = np
      }
      scene.publish(Scene.SlicingPosition.PointChanged(this))
    }

    private var _precision: Precision.Value = Precision.MmWhole
    def precision = _precision
    def precision_=(np: Precision.Value): Unit = {
      if (precision != np) {
        _precision = np
        scene.publish(Scene.SlicingPosition.PrecisionChanged(this))
      }
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
        scene.publish(Scene.SlicingPosition.BoundingBoxChanged(this))
      }
    }

    private[Scene] def updateBoundingBox() = {
      boundingBox = scene.viewports.foldLeft(BoundingBox.Zero)({case (bb, vp) => bb.union(vp.currentBoundingBox)})
    }
  }
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
    case Viewport.BoundingBoxChanged(v) => {
      slicingPosition.updateBoundingBox()
    }
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

  lazy val slicingPosition: Scene.SlicingPosition = new Scene.SlicingPosition(this)
}

class AuxiliaryObjects()(implicit override val scene: Scene) extends StandaloneSceneTreeObjectContainer[Visualizable[_]] {
  name = "Auxiliary Objects"
  override lazy val isNameUserModifiable = false
  override lazy val parent = scene
}
