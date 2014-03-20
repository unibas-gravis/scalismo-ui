package org.statismo.stk.ui

import java.io.File

import scala.swing.event.Event
import scala.util.Try

import org.statismo.stk.core.io.MeshIO
import org.statismo.stk.core.mesh.TriangleMesh
import org.statismo.stk.ui.visualization._
import scala.collection.immutable.Seq
import scala.Tuple2
import org.statismo.stk.ui.visualization.props.ColorProperty

object Mesh extends SimpleVisualizationFactory[Mesh] {
  case class GeometryChanged(source: Mesh) extends Event
  visualizations += Tuple2("org.statismo.stk.ui.ThreeDViewport", Seq(new ThreeDVisualization(None)))

  class ThreeDVisualization(from: Option[ThreeDVisualization]) extends Visualization[Mesh] {
    val color:ColorProperty = if (from.isDefined) from.get.color.derive() else new ColorProperty
    protected def createDerived() = new ThreeDVisualization(Some(this))

    protected def instantiateRenderables(source: Mesh) = {
      Seq(new MeshRenderable(source, color))
    }
  }

  class MeshRenderable(val mesh: Mesh, val color: ColorProperty) extends Renderable
}


trait Mesh extends ThreeDRepresentation[Mesh] with Displayable with Colorable with Landmarkable with Saveable {
  def peer: TriangleMesh

  override def saveToFile(file: File): Try[Unit] = {
    MeshIO.writeMesh(peer, file)
  }

  override lazy val saveableMetadata = StaticMesh

  override def parentVisualizationProvider: VisualizationProvider[Mesh] = Mesh
}