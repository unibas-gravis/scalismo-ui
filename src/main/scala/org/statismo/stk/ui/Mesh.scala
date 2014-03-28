package org.statismo.stk.ui

import java.io.File

import scala.swing.event.Event
import scala.util.Try

import org.statismo.stk.core.io.MeshIO
import org.statismo.stk.core.mesh.TriangleMesh
import org.statismo.stk.ui.visualization._
import scala.collection.immutable.Seq
import scala.Tuple2
import org.statismo.stk.ui.visualization.props.{OpacityProperty, HasColorAndOpacity, ColorProperty}

object Mesh extends SimpleVisualizationFactory[Mesh] {
  case class GeometryChanged(source: Mesh) extends Event
  visualizations += Tuple2(Viewport.ThreeDViewportClassName, Seq(new Visualization3D(None)))
  visualizations += Tuple2(Viewport.TwoDViewportClassName, Seq(new Visualization3D(None)))

  class Visualization3D(from: Option[Visualization3D]) extends Visualization[Mesh] with HasColorAndOpacity {
    override val color:ColorProperty = if (from.isDefined) from.get.color.derive() else new ColorProperty(None)
    override val opacity:OpacityProperty = if (from.isDefined) from.get.opacity.derive() else new OpacityProperty(None)

    protected def createDerived() = new Visualization3D(Some(this))

    protected def instantiateRenderables(source: Mesh) = {
      Seq(new MeshRenderable3D(source, color, opacity))
    }
  }

  class MeshRenderable3D(val mesh: Mesh, override val color: ColorProperty, override val opacity: OpacityProperty) extends Renderable with HasColorAndOpacity
}


trait Mesh extends ThreeDRepresentation[Mesh] with Landmarkable with Saveable {
  def peer: TriangleMesh

  override def saveToFile(file: File): Try[Unit] = {
    MeshIO.writeMesh(peer, file)
  }

  override lazy val saveableMetadata = StaticMesh

  override def visualizationProvider: VisualizationProvider[Mesh] = Mesh
}