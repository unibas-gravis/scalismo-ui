package org.statismo.stk.ui

import java.io.File
import scala.util.Try
import org.statismo.stk.core.io.ImageIO
import org.statismo.stk.core.common.ScalarValue
import scala.reflect.ClassTag
import reflect.runtime.universe.{TypeTag, typeOf, Type}
import scala.language.existentials
import org.statismo.stk.ui.visualization._
import org.statismo.stk.core.image.DiscreteScalarImage3D
import scala.Tuple2
import org.statismo.stk.core.geometry.Point3D
import scala.swing.Reactor
import scala.collection.immutable.HashMap
import scala.collection.immutable

object Image3DVisualizationFactory {
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

  def getInstance[A: TypeTag]() : Image3DVisualizationFactory[A] = {
    val tpe = typeOf[A]
    val instanceOption = _instances.get(tpe)
    instanceOption.getOrElse{
      val newInstance = new Image3DVisualizationFactory[A]
      _instances += Tuple2(tpe, newInstance)
      newInstance
    }.asInstanceOf[Image3DVisualizationFactory[A]]
  }

}

class Image3DVisualizationFactory[A] private[ui] extends SimpleVisualizationFactory[Image3D[A]] {
  import Image3DVisualizationFactory._
  visualizations += Tuple2(Viewport.ThreeDViewportClassName, Seq(new Visualization3D[A]))
  visualizations += Tuple2(Viewport.TwoDViewportClassName, Seq(new Visualization2D[A]))
}

class Image3D[S: ScalarValue : ClassTag : TypeTag](val peer: DiscreteScalarImage3D[S]) extends ThreeDRepresentation[Image3D[S]] with Landmarkable with Saveable {
  protected[ui] override lazy val saveableMetadata = StaticImage3D

  protected[ui] override lazy val visualizationProvider = Image3DVisualizationFactory.getInstance()

  protected[ui] lazy val asFloatImage: DiscreteScalarImage3D[Float] = peer.map[Float](p => implicitly[ScalarValue[S]].toFloat(p))

  override def saveToFile(f: File) = Try[Unit] {
    ImageIO.writeImage(peer, f)
  }

  override def addLandmarkAt(point: Point3D) = {
    parent.asInstanceOf[ThreeDObject].landmarks.addAt(point)
  }
}
