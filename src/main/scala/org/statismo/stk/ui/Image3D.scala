package org.statismo.stk.ui

import java.io.File

import org.statismo.stk.core.geometry.{Point, _3D}
import org.statismo.stk.core.image.DiscreteScalarImage
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
import spire.math.Numeric

object Image3DVisualizationFactory {

  class Visualization3D[A] extends Visualization[Image3D[A]] {
    override protected def createDerived() = new Visualization3D

    override protected def instantiateRenderables(source: Image3D[A]) = {
      Seq(new Renderable3D(source))
    }

    override val description = "Slices"
  }

  class Visualization2D[A] extends Visualization[Image3D[A]] {
    override protected def createDerived() = new Visualization2D

    override protected def instantiateRenderables(source: Image3D[A]) = {
      Seq(new Renderable2D(source))
    }

    override val description = "Slice"
  }

  private[Image3DVisualizationFactory] class BaseRenderable[A](source: Image3D[A]) extends Renderable with Reactor {
    private var _imageOrNone: Option[Image3D[_]] = Some(source)

    def imageOrNone = _imageOrNone

    listenTo(source)
    reactions += {
      case SceneTreeObject.Destroyed(s) => _imageOrNone = None
    }
  }

  class Renderable3D[A](source: Image3D[A]) extends BaseRenderable(source)

  class Renderable2D[A](source: Image3D[A]) extends BaseRenderable(source)

  private var _instances = new immutable.HashMap[Type, Image3DVisualizationFactory[_]]

  def getInstance[A: TypeTag](): Image3DVisualizationFactory[A] = {
    val tpe = typeOf[A]
    val instanceOption = _instances.get(tpe)
    instanceOption.getOrElse {
      val newInstance = new Image3DVisualizationFactory[A]
      _instances += Tuple2(tpe, newInstance)
      newInstance
    }.asInstanceOf[Image3DVisualizationFactory[A]]
  }

}

class Image3DVisualizationFactory[A] private[ui] extends SimpleVisualizationFactory[Image3D[A]] {

  import org.statismo.stk.ui.Image3DVisualizationFactory._

  visualizations += Tuple2(Viewport.ThreeDViewportClassName, Seq(new Visualization3D[A]))
  visualizations += Tuple2(Viewport.TwoDViewportClassName, Seq(new Visualization2D[A]))
}


object Image3D {

  case class Reloaded(source: Image3D[_]) extends Event

}

class Image3D[S: Numeric : ClassTag : TypeTag](reloader: Reloader[DiscreteScalarImage[_3D, S]]) extends ThreeDRepresentation[Image3D[S]] with Landmarkable with Saveable with Reloadable {

  private var _peer = reloader.load().get

  def peer: DiscreteScalarImage[_3D, S] = _peer

  protected[ui] override lazy val saveableMetadata = StaticImage3D

  protected[ui] override lazy val visualizationProvider = Image3DVisualizationFactory.getInstance()

  protected[ui] def asFloatImage: DiscreteScalarImage[_3D, Float] = peer.map[Float](p => implicitly[Numeric[S]].toFloat(p))

  override def saveToFile(f: File) : Try[Unit] = {
    val extension = {
      val dot = f.getName.lastIndexOf('.')
      // dot == 0 example: ".file"
      if (dot > 0) Success(f.getName.substring(dot + 1)) else Failure(new IllegalArgumentException("No file extension given"))
    }
    extension flatMap {
      case "vtk" => ImageIO.writeVTK[_3D,S](peer, f)
      case "nii" | "nia" => ImageIO.writeNifti(peer, f)
      case _ => Failure(new IllegalArgumentException("Unsupported file extension: " + extension.get))
    }
  }

  override def addLandmarkAt(point: Point[_3D], nameOpt: Option[String]) = {
    parent.asInstanceOf[ThreeDObject].landmarks.addAt(point, nameOpt)
  }

  override def reload() = {
    reloader.load() match {
      case (Success(newPeer)) =>
        if (newPeer != _peer) {
          _peer = newPeer
          publishEdt(Image3D.Reloaded(this))
        }
        Success(())
      case Failure(ex) => Failure(ex)
    }
  }

  override def isCurrentlyReloadable = reloader.isReloadable
}
