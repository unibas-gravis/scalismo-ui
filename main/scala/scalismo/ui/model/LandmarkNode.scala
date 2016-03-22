package scalismo.ui.model

import java.awt.Color
import java.io.File

import scalismo.geometry.{ Point3D, Landmark, _3D }
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

class LandmarksNode(override val parent: GroupNode) extends SceneNodeCollection[LandmarkNode] with Loadable with Saveable {
  override val name: String = "Landmarks"

  val nameGenerator = new NameGenerator

  def add(landmark: Landmark[_3D]): LandmarkNode = {
    val node = new LandmarkNode(this, landmark)
    add(node)
    node
  }

  // convenience method which constructs the landmark on the fly
  def add(point: Point3D, name: String, uncertainty: Uncertainty): LandmarkNode = {
    val landmark = new Landmark[_3D](name, point, uncertainty = Some(uncertainty.to3DNormalDistribution))
    add(landmark)
  }

  override def loadMetadata: FileIoMetadata = FileIoMetadata.Landmarks

  override def saveMetadata: FileIoMetadata = FileIoMetadata.Landmarks

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

  override def save(file: File): Try[Unit] = {
    saveNodes(children, file)
  }

  def saveNodes(nodes: List[LandmarkNode], file: File): Try[Unit] = {
    require(nodes.forall(_.parent == this))

    val landmarks = nodes.map { node =>
      // landmark may have been renamed and / or transformed
      node.transformedSource.copy(id = node.name, uncertainty = Some(node.uncertainty.value.to3DNormalDistribution))
    }
    val ok = if (FileUtil.extension(file) == "csv") {
      LandmarkIO.writeLandmarksCsv(landmarks, file)
    } else {
      LandmarkIO.writeLandmarksJson(landmarks, file)
    }
    ok
  }
}

class LandmarkNode(override val parent: LandmarksNode, override val source: Landmark[_3D]) extends Transformable[Landmark[_3D]] with InverseTransformation with Removeable with Renameable with HasUncertainty with HasColor with HasOpacity with HasLineWidth {
  name = source.id

  override val color = new ColorProperty(Color.BLUE)
  override val opacity = new OpacityProperty()
  override val lineWidth = new LineWidthProperty()
  override val uncertainty = new UncertaintyProperty(source.uncertainty.map(Uncertainty.apply).getOrElse(Uncertainty.DefaultUncertainty))

  override def remove(): Unit = parent.remove(this)

  override def transform(untransformed: Landmark[_3D], transformation: PointTransformation): Landmark[_3D] = {
    untransformed.copy(point = transformation(untransformed.point))
  }

  override def inverseTransform(point: Point3D): Point3D = {
    source.point
  }

  override def group: GroupNode = parent.parent
}

