package org.statismo.stk.ui

import java.io.File

import org.statismo.stk.core.common.ScalarValue
import org.statismo.stk.core.geometry.Point3D
import org.statismo.stk.core.image.DiscreteScalarImage3D
import org.statismo.stk.core.io.ImageIO
import org.statismo.stk.ui.Reloadable.Reloader
import org.statismo.stk.ui.visualization._

import scala.collection.immutable
import scala.language.existentials
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.{Type, TypeTag, typeOf}
import scala.swing.Reactor
import scala.swing.event.Event
import scala.util.{Failure, Success, Try}

object Image3D {

  case class Reloaded(source: Image3D[_]) extends Event

  object VisualizationFactory {

    class Visualization3D[A] extends Visualization[Image3D[A]] {
      override protected def createDerived() = new Visualization3D

      override protected def instantiateRenderables(source: Image3D[A]) = {
        Seq(new Renderable3D(source))
      }
    }

    class Visualization2D[A] extends Visualization[Image3D[A]] {
      override protected def createDerived() = new Visualization2D

      override protected def instantiateRenderables(source: Image3D[A]) = {
        Seq(new Renderable2D(source))
      }
    }

    private[VisualizationFactory] class BaseRenderable[A](source: Image3D[A]) extends Renderable with Reactor {
      private var _imageOrNone: Option[Image3D[_]] = Some(source)

      def imageOrNone = _imageOrNone

      listenTo(source)
      reactions += {
        case SceneTreeObject.Destroyed(s) => _imageOrNone = None
      }
    }

    class Renderable3D[A](source: Image3D[A]) extends BaseRenderable(source)

    class Renderable2D[A](source: Image3D[A]) extends BaseRenderable(source)

    private var _instances = new immutable.HashMap[Type, VisualizationFactory[_]]

    def getInstance[A: TypeTag](): VisualizationFactory[A] = {
      val tpe = typeOf[A]
      val instanceOption = _instances.get(tpe)
      instanceOption.getOrElse {
        val newInstance = new VisualizationFactory[A]
        _instances += Tuple2(tpe, newInstance)
        newInstance
      }.asInstanceOf[VisualizationFactory[A]]
    }
  }

  class VisualizationFactory[A] private[ui] extends SimpleVisualizationFactory[Image3D[A]] {

    import org.statismo.stk.ui.Image3D.VisualizationFactory._

    visualizations += Tuple2(Viewport.ThreeDViewportClassName, Seq(new Visualization3D[A]))
    visualizations += Tuple2(Viewport.TwoDViewportClassName, Seq(new Visualization2D[A]))
  }
}

class Image3D[S: ScalarValue : ClassTag : TypeTag](initialPeer: DiscreteScalarImage3D[S], reloaderOption: Option[Reloader[DiscreteScalarImage3D[S]]]) extends ThreeDRepresentation[Image3D[S]] with Landmarkable with Saveable with Reloadable {

  private var _peer = initialPeer
  def peer: DiscreteScalarImage3D[S] = _peer

  protected[ui] override lazy val saveableMetadata = StaticImage3D

  protected[ui] override lazy val visualizationProvider = Image3D.VisualizationFactory.getInstance()

  protected[ui] def asFloatImage: DiscreteScalarImage3D[Float] = peer.map[Float](p => implicitly[ScalarValue[S]].toFloat(p))

  override def saveToFile(f: File) = Try[Unit] {
    ImageIO.writeImage(peer, f)
  }

  override def addLandmarkAt(point: Point3D) = {
    parent.asInstanceOf[ThreeDObject].landmarks.addAt(point)
  }
  override def reload() = {
    reloaderOption match {
      case Some(rld) =>
        rld.load() match {
          case (Success(newPeer)) =>
            if (newPeer != _peer) {
              _peer = newPeer
              publishEdt(Image3D.Reloaded(this))
            }
            Success(())
          case Failure(ex) => Failure(ex)
        }
      case None => Failure(new IllegalStateException("not reloadable"))
    }
  }

  override def isCurrentlyReloadable = reloaderOption.isDefined
}
