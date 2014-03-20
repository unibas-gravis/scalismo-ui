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
  visualizations += Tuple2(Viewport.ThreeDViewportClassName, Seq(new ThreeDVisualization(None)))

  class ThreeDVisualization(from: Option[ThreeDVisualization]) extends Visualization[Mesh] with HasColorAndOpacity {
    override val color:ColorProperty = if (from.isDefined) from.get.color.derive() else new ColorProperty
    override val opacity:OpacityProperty = if (from.isDefined) from.get.opacity.derive() else new OpacityProperty

    protected def createDerived() = new ThreeDVisualization(Some(this))

    protected def instantiateRenderables(source: Mesh) = {
      Seq(new ThreeDMeshRenderable(source, color, opacity))
    }
  }

  class ThreeDMeshRenderable(val mesh: Mesh, override val color: ColorProperty, override val opacity: OpacityProperty) extends Renderable with HasColorAndOpacity
}


trait Mesh extends ThreeDRepresentation[Mesh] with Landmarkable with Saveable {
  def peer: TriangleMesh

  override def saveToFile(file: File): Try[Unit] = {
    MeshIO.writeMesh(peer, file)
  }

  override lazy val saveableMetadata = StaticMesh

  override def parentVisualizationProvider: VisualizationProvider[Mesh] = Mesh
}