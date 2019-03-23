/*
 * Copyright (C) 2016  University of Basel, Graphics and Vision Research Group 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scalismo.ui.model

import java.awt.Color
import java.io.File

import scalismo.geometry.{ Landmark, Point3D, _3D }
import scalismo.io.LandmarkIO
import scalismo.ui.model.LandmarksNode.NameGenerator
import scalismo.ui.model.capabilities._
import scalismo.ui.model.properties._
import scalismo.ui.util.{ FileIoMetadata, FileUtil }

import scala.util.{ Failure, Success, Try }

object LandmarksNode {

  class NameGenerator {

    final val Prefixes = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    private var prefix = 0
    private var suffix = 0

    def nextName() = {
      val p = Prefixes(prefix)
      val name = if (suffix == 0) p.toString else s"${p}_$suffix"
      prefix = (prefix + 1) % Prefixes.length()
      if (prefix == 0) suffix += 1

      name
    }

    def reset() = {
      prefix = 0
      suffix = 0
    }
  }

}

class LandmarksNode(override val parent: GroupNode) extends SceneNodeCollection[LandmarkNode] with Loadable {
  override val name: String = "Landmarks"

  val nameGenerator = new NameGenerator

  def add(landmark: Landmark[_3D]): LandmarkNode = {
    val node = new LandmarkNode(this, landmark)
    add(node)
    node
  }

  // convenience method which constructs the landmark on the fly
  def add(point: Point3D, name: String, uncertainty: Uncertainty): LandmarkNode = {
    val landmark = new Landmark[_3D](name, point, uncertainty = Some(uncertainty.toMultivariateNormalDistribution))
    add(landmark)
  }

  override def loadMetadata: FileIoMetadata = FileIoMetadata.Landmarks

  override def load(file: File): Try[Unit] = {
    val read = if (FileUtil.extension(file) == "csv") {
      LandmarkIO.readLandmarksCsv[_3D] _
    } else {
      LandmarkIO.readLandmarksJson[_3D] _
    }

    read(file) match {
      case Success(landmarks) =>
        landmarks.foreach(add)
        Success(())
      case Failure(ex) => Failure(ex)
    }
  }

  def saveNodes(nodes: List[LandmarkNode], file: File, transformedFlag: Boolean = true): Try[Unit] = {
    require(nodes.forall(_.parent == this))

    val landmarks = nodes.map { node =>
      // landmark may have been renamed and / or transformed
      if (transformedFlag)
        node.transformedSource.copy(id = node.name, uncertainty = Some(node.uncertainty.value.toMultivariateNormalDistribution))
      else
        node.source.copy(id = node.name, uncertainty = Some(node.uncertainty.value.toMultivariateNormalDistribution))
    }
    val ok = if (FileUtil.extension(file) == "csv") {
      LandmarkIO.writeLandmarksCsv(landmarks, file)
    } else {
      LandmarkIO.writeLandmarksJson(landmarks, file)
    }
    ok
  }
}

class LandmarkNode(override val parent: LandmarksNode, sourceLm: Landmark[_3D]) extends Transformable[Landmark[_3D]] with InverseTransformation with Removeable with Renameable with HasUncertainty with HasColor with HasOpacity with HasLineWidth with HasPickable {
  name = sourceLm.id

  override val color = new ColorProperty(Color.BLUE)
  override val opacity = new OpacityProperty()
  override val lineWidth = new LineWidthProperty()
  override val pickable = new PickableProperty()

  // lazy is needed here since traits such as Transformable call source() which need uncertainty, all this at *construction time*
  override lazy val uncertainty = new UncertaintyProperty(sourceLm.uncertainty.map(Uncertainty.apply).getOrElse(Uncertainty.DefaultUncertainty))

  // when requesting the source, we make sure that the returned id is the current name (in case it was renamed), same for the uncertainty
  override def source = Landmark(name, sourceLm.point, sourceLm.description, Some(uncertainty.value.toMultivariateNormalDistribution))

  override def remove(): Unit = parent.remove(this)

  override def transform(untransformed: Landmark[_3D], transformation: PointTransformation): Landmark[_3D] = {
    untransformed.copy(point = transformation(untransformed.point))
  }

  override def inverseTransform(point: Point3D): Point3D = {
    source.point
  }

  override def group: GroupNode = parent.parent
}

