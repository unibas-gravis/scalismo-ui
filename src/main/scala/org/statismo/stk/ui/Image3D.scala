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
import org.statismo.stk.ui.visualization.{VisualizationProvider, SimpleVisualizationFactory}

object Image3D extends SimpleVisualizationFactory[Image3D[_]] {
}

class Image3D[S : ScalarValue : ClassTag: TypeTag](val peer: DiscreteScalarImage3D[S]) extends ThreeDRepresentation[Image3D[S]] with Saveable {
  override lazy val saveableMetadata = StaticImage3D
  override def visualizationProvider: VisualizationProvider[Image3D[S]] = Image3D

  def saveToFile(f: File) = Try[Unit] {
      ImageIO.writeImage(peer, f)
  }
}
