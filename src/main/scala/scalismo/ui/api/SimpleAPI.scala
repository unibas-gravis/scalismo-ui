package scalismo.ui.api

import breeze.linalg.DenseVector
import scalismo.common.{ DiscreteScalarField, Scalar }
import scalismo.geometry.{ Dim, Landmark, Point, _3D }
import scalismo.image.DiscreteScalarImage
import scalismo.mesh.TriangleMesh
import scalismo.statisticalmodel.StatisticalMeshModel
import scalismo.statisticalmodel.asm.ASMSample
import scalismo.ui._
import scalismo.ui.swing.{ ScalismoFrame, ScalismoLookAndFeel }
import scalismo.ui.util.EdtUtil
import spire.math.Numeric

import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

/**
 * Defines a minimal set of methods to easily interact with an stk.ui scene in order to display and retrieve objects.
 * When using this API, objects in the scene should be identified by a unique name.
 */

trait SimpleAPI {

  def scene: Scene

  /**
   * Adds the indicated list of landmarks to the first object in the scene with a matching name
   *
   * @param landmarks an indexedSeq of landmarks to add
   * @param sceneObjectName The scene name associated to the object to which to add the landmarks
   */
  def addLandmarksTo(landmarks: Iterable[Landmark[_3D]], sceneObjectName: String): Unit = {
    val genericOpt = scene.find[ThreeDRepresentation[_] with Landmarkable](_.name == sceneObjectName).headOption
    if (genericOpt.isDefined) {
      genericOpt.foreach(a => landmarks.foreach { l => a.addLandmarkAt(l.point, Some(l.id)) })
    } else {
      scene.find[ShapeModelView](_.name == sceneObjectName).headOption.foreach { a =>
        landmarks.foreach { l => a.instances(0).meshRepresentation.addLandmarkAt(l.point, Some(l.id)) }
      }
    }
  }

  /**
   * Returns the sequence of landmarks associated with the first scene object with a matching name
   *
   * @param name of the object from which to extract landmarks
   */
  def getLandmarksOf(name: String): Option[Seq[Landmark[_3D]]] = {
    val t = scene.find[StaticThreeDObject](_.name == name).headOption.map(a => a.landmarks)
      .map(d => d.map { case a => Landmark(a.name, a.point, None, Some(Uncertainty.toNDimensionalNormalDistribution(a.uncertainty))) })

    if (t.isDefined) t
    else
      scene.find[ShapeModelView](_.name == name).headOption.map(a => a.instances(0).landmarks)
        .map(d => d.map { case a => Landmark(a.name, a.point, None, Some(Uncertainty.toNDimensionalNormalDistribution(a.uncertainty))) })
  }

  /**
   * Removes all objects with a matching name from the scene
   *
   * @param name of the object to remove
   */
  def remove(name: String): Unit = {
    val l = scene.find[SceneTreeObject with Removeable](s => s.name == name)
    l.foreach(a => a.remove())
  }

  /**
   * Adds a discrete image to the scene and attributes it the indicated name
   *
   * @param image The discrete image to be added
   * @param name to associate to the image
   * @tparam P The type of the pixel (Short, Int, Float, Double).
   */
  def showImage[D <: Dim, P: Numeric: ClassTag: TypeTag](image: DiscreteScalarImage[D, P], name: String)(implicit show: Show[DiscreteScalarImage[D, P]]): Unit = {
    show.show(image, name)(scene)
  }

  /**
   * Adds a point cloud to the scene and attributes it the indicated name
   *
   * @param pc An Iterable containing the points to be added
   * @param name to associate to the point cloud
   */
  def showPointCloud(pc: Iterable[Point[_3D]], name: String): Unit = {
    show[Iterable[Point[_3D]]](pc, name)
  }

  /**
   * Adds a mesh to the scene and attributes it the indicated name
   *
   * @param m The triangle mesh to be added
   * @param name to associate to the mesh
   */
  def showMesh(m: TriangleMesh, name: String): Unit = {
    show[TriangleMesh](m, name)
  }

  /**
   * Adds a Statistical Mesh Model to the scene and attributes it the indicated name
   *
   * @param sm The model to be added
   * @param name to associate to the model
   */
  def showModel(sm: StatisticalMeshModel, name: String): Unit = {
    show[StatisticalMeshModel](sm, name)
  }

  def showScalarField[A: Scalar: ClassTag: TypeTag](sf: DiscreteScalarField[_3D, A], name: String): Unit = {
    show[DiscreteScalarField[_3D, A]](sf, name)
  }

  def showScalarMeshField[A: Scalar: ClassTag: TypeTag](smf: scalismo.mesh.ScalarMeshField[A], name: String): Unit = {
    show[scalismo.mesh.ScalarMeshField[A]](smf, name)
  }

  def getCoefficientsOf(modelName: String): Option[IndexedSeq[Float]] = {
    scene.find[ShapeModelView](_.name == modelName).headOption.map(a => a.instances(0).coefficients)
  }

  def setCoefficientsOf(modelName: String, coefficients: DenseVector[Float]) = {
    scene.find[ShapeModelView](_.name == modelName).headOption.foreach(a => a.instances(0).coefficients = coefficients.toArray.toIndexedSeq)
  }

  def showASMSample(sample: ASMSample, name: String): Unit = show[ASMSample](sample, name)

  def show[A](a: A, name: String)(implicit show: Show[A]) = show.show(a, name)(scene)

}

object SimpleAPI {

  class ScalismoUI private (val frame: ScalismoFrame) extends SimpleAPI {
    override val scene: Scene = frame.scene

    def close() = frame.closeOperation()

  }

  object ScalismoUI {

    ScalismoLookAndFeel.initializeWith(ScalismoLookAndFeel.defaultLookAndFeelClassName)

    def apply(): ScalismoUI = {

      val frame = EdtUtil.onEdtWithResult {
        val frame = new ScalismoFrame(new Scene())
        frame.startup(Array())
        frame.visible = true
        frame
      }

      new ScalismoUI(frame)
    }
  }

}
