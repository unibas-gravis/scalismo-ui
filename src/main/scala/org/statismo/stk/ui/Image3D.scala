package org.statismo.stk.ui

import java.io.File
import scala.util.{Success, Failure, Try}
import org.statismo.stk.core.image.DiscreteScalarImage3D
import org.statismo.stk.core.io.ImageIO
import scala.swing.event.Event
import org.statismo.stk.core.geometry.Point3D
import org.statismo.stk.core.common.ScalarValue
import scala.reflect.ClassTag
import reflect.runtime.universe.{TypeTag, typeOf}
import scala.language.existentials
import org.statismo.stk.ui.visualization._
import org.statismo.stk.core.image.DiscreteScalarImage3D
import scala.Tuple2
import org.statismo.stk.core.geometry.Point3D

object Image3D extends SimpleVisualizationFactory[Image3D[_]] {
  visualizations += Tuple2(Viewport.ThreeDViewportClassName, Seq(new Visualization3D))
  visualizations += Tuple2(Viewport.TwoDViewportClassName, Seq(new Visualization2D))

  class Visualization3D extends Visualization[Image3D[_]] {
    override protected def createDerived() = new Visualization3D

    override protected def instantiateRenderables(source: Image3D[_]) = {
      Seq(new Renderable3D(source))
    }
  }

  class Visualization2D extends Visualization[Image3D[_]] {
    override protected def createDerived() = new Visualization2D

    override protected def instantiateRenderables(source: Image3D[_]) = {
      Seq(new Renderable2D(source))
    }
  }

  class Renderable3D(val source: Image3D[_]) extends Renderable {
  }

  class Renderable2D(val source: Image3D[_]) extends Renderable {
  }
}

class Image3D[S : ScalarValue : ClassTag: TypeTag](val peer: DiscreteScalarImage3D[S]) extends ThreeDRepresentation[Image3D[S]] with Landmarkable with Saveable {
  protected[ui] override lazy val saveableMetadata = StaticImage3D
  protected[ui] override def visualizationProvider: VisualizationProvider[Image3D[S]] = Image3D
  protected[ui] lazy val asFloatImage: DiscreteScalarImage3D[Float] = peer.map[Float](p => implicitly[ScalarValue[S]].toFloat(p))

  override def saveToFile(f: File) = Try[Unit] {
      ImageIO.writeImage(peer, f)
  }

  override def addLandmarkAt(point: Point3D) = {
    parent.asInstanceOf[ThreeDObject].landmarks.addAt(point)
  }
}
