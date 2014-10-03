package org.statismo.stk.ui

import org.statismo.stk.core.geometry.Point3D
import org.statismo.stk.ui.visualization._

import scala.collection.immutable
import scala.collection.immutable.List
import scala.swing.event.Event
import scala.util.Try

object Scene {

  case class TreeTopologyChanged private[Scene](scene: Scene) extends Event

  case class PerspectiveChanged private[Scene](scene: Scene) extends Event

  case class PerspectiveChangeCompleted private[Scene](scene: Scene) extends Event

  case class VisibilityChanged private[Scene](scene: Scene) extends Event

  object SlicingPosition extends SimpleVisualizationFactory[SlicingPosition] {

    case class BoundingBoxChanged(slicingPosition: SlicingPosition) extends Event

    case class PointChanged(slicingPosition: SlicingPosition) extends Event

    case class PrecisionChanged(slicingPosition: SlicingPosition) extends Event

    case class VisibilityChanged(slicingPosition: SlicingPosition) extends Event

    object Precision extends Enumeration {

      import scala.language.implicitConversions

      case class Val(name: String, format: Float => String, toIntValue: Float => Int, fromInt: Int => Float) extends super.Val(nextId, name)

      implicit def valueToPrecisionVal(x: Value) = x.asInstanceOf[Val]

      val MmWhole = new Val("1 mm", {
        value => f"$value%1.0f"
      }, {
        f => Math.round(f)
      }, {
        i => i.toFloat
      })
      val MmTenth = new Val("1/10 mm", {
        value => f"$value%1.1f"
      }, {
        f => Math.round(f * 10)
      }, {
        i => i.toFloat / 10f
      })
      val MmHundredth = new Val("1/100 mm", {
        value => f"$value%1.2f"
      }, {
        f => Math.round(f * 100)
      }, {
        i => i.toFloat / 100f
      })
      val MmThousandth = new Val("1/1000 mm", {
        value => f"$value%1.3f"
      }, {
        f => Math.round(f * 1000)
      }, {
        i => i.toFloat / 1000f
      })
    }

    visualizations += Tuple2(Viewport.ThreeDViewportClassName, Seq(new Visualization3D))
    visualizations += Tuple2(Viewport.TwoDViewportClassName, Seq(new Visualization2D))

    class Visualization3D extends Visualization[SlicingPosition] {
      override protected def createDerived() = new Visualization3D

      override protected def instantiateRenderables(source: SlicingPosition) = immutable.Seq(
        new BoundingBoxRenderable3D(source),
        new SlicingPlaneRenderable3D(source, Axis.X),
        new SlicingPlaneRenderable3D(source, Axis.Y),
        new SlicingPlaneRenderable3D(source, Axis.Z)
      )
    }

    class Visualization2D extends Visualization[SlicingPosition] {
      override protected def createDerived() = new Visualization2D

      override protected def instantiateRenderables(source: SlicingPosition) = immutable.Seq(
        new SlicingPlaneRenderable2D(source)
      )
    }

    class BoundingBoxRenderable3D(val source: SlicingPosition) extends Renderable

    class SlicingPlaneRenderable3D(val source: SlicingPosition, val axis: Axis.Value) extends Renderable

    class SlicingPlaneRenderable2D(val source: SlicingPosition) extends Renderable

  }

  class SlicingPosition(val scene: Scene) extends Visualizable[SlicingPosition] {

    import org.statismo.stk.ui.Scene.SlicingPosition.Precision

    protected[ui] override def visualizationProvider = SlicingPosition

    protected[ui] override def isVisibleIn(viewport: Viewport) = _visible || viewport.isInstanceOf[TwoDViewport]

    private var _visible = false

    def visible = _visible

    def visible_=(nv: Boolean) = {
      if (_visible != nv) {
        _visible = nv
        scene.publish(Scene.SlicingPosition.VisibilityChanged(this))
        scene.publish(Scene.VisibilityChanged(scene))
      }
    }

    private var _point: Option[Point3D] = None

    def point = this.synchronized {
      _point.getOrElse(Point3D((boundingBox.xMin + boundingBox.xMax) / 2, (boundingBox.yMin + boundingBox.yMax) / 2, (boundingBox.zMin + boundingBox.zMax) / 2))
    }

    private def point_=(np: Point3D) = this.synchronized {
      if (!_point.isDefined || _point.get != np) {
        _point = Some(np)
      }
      scene.publishEdt(Scene.SlicingPosition.PointChanged(this))
    }

    private var _precision: Precision.Value = Precision.MmWhole

    def precision = _precision

    def precision_=(np: Precision.Value): Unit = {
      if (precision != np) {
        _precision = np
        scene.publishEdt(Scene.SlicingPosition.PrecisionChanged(this))
      }
    }

    def x = this.synchronized {
      point.x
    }

    def y = this.synchronized {
      point.y
    }

    def z = this.synchronized {
      point.z
    }

    def x_=(nv: Float) = this.synchronized {
      val sv = Math.min(Math.max(boundingBox.xMin, nv), boundingBox.xMax)
      if (x != sv) {
        point_=(new Point3D(sv, y, z))
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
        point = new Point3D(x, y, sv)
      }
    }

    private def revalidatePoint() = this.synchronized {
      val sx = Math.min(Math.max(boundingBox.xMin, x), boundingBox.xMax)
      val sy = Math.min(Math.max(boundingBox.yMin, y), boundingBox.yMax)
      val sz = Math.min(Math.max(boundingBox.zMin, z), boundingBox.zMax)
      if (x != sx || y != sy || z != sz) {
        point = new Point3D(sx, sy, sz)
      }
    }

    private var _boundingBox = BoundingBox.None

    def boundingBox = this.synchronized(_boundingBox)

    private[Scene] def boundingBox_=(nb: BoundingBox) = this.synchronized {
      if (boundingBox != nb) {
        _boundingBox = nb
        scene.publishEdt(Scene.SlicingPosition.BoundingBoxChanged(this))
      }
      revalidatePoint()
    }

    private[Scene] def updateBoundingBox() = {
      boundingBox = scene.viewports.foldLeft(BoundingBox.None)({
        case (bb, vp) =>
          bb.union(vp.currentBoundingBox)
      })
    }
  }

}

class Scene extends SceneTreeObject {
  deafTo(this)

  org.statismo.stk.core.initialize()

  name = "Scene"
  protected[ui] override lazy val isNameUserModifiable = false

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
      publishEdt(Scene.PerspectiveChanged(this))
    }
  }

  protected[ui] def publishPerspectiveChangeCompleted() = {
    publishEdt(Scene.PerspectiveChangeCompleted(this))
  }

  protected[ui] def publishVisibilityChanged() = {
    publishEdt(Scene.VisibilityChanged(this))
  }

  protected[ui] def viewports = perspective.viewports

  val shapeModels = new ShapeModels
  val staticObjects = new StaticThreeDObjects
  val auxiliaryObjects = new AuxiliaryObjects

  protected[ui] override val children = List(shapeModels, staticObjects) //, auxiliaries)

  def tryLoad(filename: String, factories: Seq[SceneTreeObjectFactory[SceneTreeObject]] = SceneTreeObjectFactory.DefaultFactories): Try[SceneTreeObject] = {
    SceneTreeObjectFactory.load(filename, factories)
  }

  reactions += {
    case Viewport.Destroyed(v) => deafTo(v)
    case Viewport.BoundingBoxChanged(v) =>
      slicingPosition.updateBoundingBox()
    case SceneTreeObject.VisibilityChanged(s) =>
      publishEdt(Scene.VisibilityChanged(this))
    case SceneTreeObject.ChildrenChanged(s) =>
      publishEdt(Scene.TreeTopologyChanged(this))
    case SceneTreeObject.Destroyed(s) =>
      deafTo(s)
    case m@Nameable.NameChanged(s) =>
      publishEdt(m)
  }


  protected override def onViewportsChanged(viewports: Seq[Viewport]) = {
    viewports.foreach(listenTo(_))
    super.onViewportsChanged(viewports)
  }

  protected[ui] lazy val visualizations: Visualizations = new Visualizations
  lazy val slicingPosition: Scene.SlicingPosition = new Scene.SlicingPosition(this)

  protected[ui] override def visualizables(filter: Visualizable[_] => Boolean = {
    o => true
  }): Seq[Visualizable[_]] = {
    Seq(super.visualizables(filter), Seq(slicingPosition).filter(filter)).flatten
  }
}

class AuxiliaryObjects()(implicit override val scene: Scene) extends StandaloneSceneTreeObjectContainer[VisualizableSceneTreeObject[_]] {
  name = "Auxiliary Objects"
  protected[ui] override lazy val isNameUserModifiable = false
  override lazy val parent = scene
}
