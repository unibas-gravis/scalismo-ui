package scalismo.ui.util

import scalismo.geometry.{ Landmark, Point, _3D }
import scalismo.image.DiscreteScalarImage
import scalismo.mesh.TriangleMesh
import scalismo.statisticalmodel.StatisticalMeshModel
import scalismo.ui.{ Landmarkable, Removeable, Scene, SceneTreeObject, ShapeModel, StaticImage3D, StaticMesh, StaticPointCloud, StaticThreeDObject, ThreeDRepresentation }
import spire.math.Numeric

import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

/**
 * Defines a minimal set of methods to easily interact with an stk.ui scene in order to display and retrieve objects.
 * When using this API, objects in the scene should be identified by a unique name.
 */

object SimpleAPI {
  /**
   * Adds the indicated list of landmarks to the first object in the scene with a matching name
   *
   * @param landmarks an indexedSeq of landmarks to add
   * @param sceneObjectName The scene name associated to the object to which to add the landmarks
   */
  def addLandmarksTo(landmarks: IndexedSeq[Landmark[_3D]], sceneObjectName: String)(implicit s: Scene) {
    val t = s.find[ThreeDRepresentation[_] with Landmarkable](_.name == sceneObjectName).headOption.map { a =>
      landmarks.foreach { l => a.addLandmarkAt(l.point, Some(l.id)) }
    }

    if (!t.isDefined) s.find[ShapeModel](_.name == sceneObjectName).headOption.map { a =>
      landmarks.foreach { l => a.instances(0).meshRepresentation.addLandmarkAt(l.point, Some(l.id))
      }
    }
  }

  /**
   * Returns the sequence of landmarks associated with the first scene object with a matching name
   *
   * @param name of the object from which to extract landmarks
   */
  def getLandmarksOf(name: String)(implicit s: Scene): Option[Seq[Landmark[_3D]]] = {
    val t = s.find[StaticThreeDObject](_.name == name).headOption.map(a => a.landmarks)
      .map(d => d.map { case a => Landmark(a.name, a.point) })

    if (t.isDefined) t
    else
      s.find[ShapeModel](_.name == name).headOption.map(a => a.instances(0).landmarks)
        .map(d => d.map { case a => Landmark(a.name, a.point) })
  }

  /**
   * Removes all objects with a matching name from the scene
   *
   * @param name of the object to remove
   */
  def remove(name: String)(implicit s: Scene) {
    val l = s.find[SceneTreeObject with Removeable](s => s.name == name)
    l.foreach(a => a.remove())
  }

  /**
   * Adds a discrete image to the scene and attributes it the indicated name
   *
   * @param image The discrete image to be added
   * @param name to associate to the image
   * @tparam P The type of the pixel (Short, Int, Float, Double).
   */
  def show[P: Numeric: ClassTag: TypeTag](image: DiscreteScalarImage[_3D, P], name: String)(implicit s: Scene) {
    StaticImage3D.createFromPeer(image, None, Some(name))
  }

  /**
   * Adds a point cloud to the scene and attributes it the indicated name
   *
   * @param pc An Iterable containing the points to be added
   * @param name to associate to the point cloud
   */
  def show(pc: Iterable[Point[_3D]], name: String)(implicit s: Scene) {
    StaticPointCloud.createFromPeer(pc.toIndexedSeq, None, Some(name))
  }

  /**
   * Adds a mesh to the scene and attributes it the indicated name
   *
   * @param m The triangle mesh to be added
   * @param name to associate to the mesh
   */
  def show(m: TriangleMesh, name: String)(implicit s: Scene) {
    StaticMesh.createFromPeer(m, None, Some(name))
  }

  /**
   * Adds a Statistical Mesh Model to the scene and attributes it the indicated name
   *
   * @param sm The model to be added
   * @param name to associate to the model
   */
  def show(sm: StatisticalMeshModel, name: String)(implicit s: Scene) {
    ShapeModel.createFromPeer(sm, 1, Some(name))
  }
}