package org.statismo.stk.ui

import org.statismo.stk.core.statisticalmodel.StatisticalMeshModel
import org.statismo.stk.core.statisticalmodel.SpecializedLowRankGaussianProcess
import org.statismo.stk.core.geometry.ThreeD
import breeze.linalg.DenseVector
import org.statismo.stk.core.mesh.TriangleMesh
import java.io.File
import scala.util.Try
import scala.util.Failure
import org.statismo.stk.core.io.StatismoIO
import scala.swing.event.Event
import org.statismo.stk.core.geometry.Point3D
import scala.collection.mutable.ArrayBuffer

class ShapeModels(implicit override val scene: Scene) extends SceneTreeObjectContainer[ShapeModel] {
  override lazy val parent = scene
  name = "Statistical Shape Models"
  override lazy val isNameUserModifiable = false

  def load(filenames: String*): Seq[ShapeModel] = {
    val loaded = filenames.map({ fn => ShapeModel(fn) }).filter(_.isSuccess).map(_.get)
    loaded
  }
}

object ShapeModel extends Loadable[ShapeModel] {
  override val description = "Statistical Shape Model (.h5)"
  override val fileExtensions = Seq("h5")

  override def apply(file: File)(implicit scene: Scene): Try[ShapeModel] = apply(file, 2)
  def apply(filename: String, numberOfInstancesToCreate: Int = 1)(implicit scene: Scene): Try[ShapeModel] = apply(new File(filename), numberOfInstancesToCreate)
  def apply(file: File, numberOfInstancesToCreate: Int)(implicit scene: Scene): Try[ShapeModel] = {
    for {
      raw <- StatismoIO.readStatismoMeshModel(file)
    } yield {
      val shape = new ShapeModel(raw)
      shape.name = file.getName()
      0 until numberOfInstancesToCreate foreach { i =>
        shape.instances.create()
      }
      shape
    }
  }

  case class LandmarksAdded(source: ShapeModel) extends Event
}

class ShapeModel(val rawModel: StatisticalMeshModel)(implicit override val scene: Scene) extends SceneTreeObject {

  private lazy val refMesh = rawModel.mesh

  lazy val gp: SpecializedLowRankGaussianProcess[ThreeD] = {
    rawModel.gp match {
      case specializedGP: SpecializedLowRankGaussianProcess[ThreeD] => specializedGP
      case gp => gp.specializeForPoints(refMesh.points.force)
    }
  }

  def calculateMesh(coefficients: IndexedSeq[Float]) = {
    val vector = DenseVector[Float](coefficients.toArray)
    val ptdefs = gp.instanceAtPoints(vector)
    val newptseq = for ((pt, df) <- ptdefs) yield pt + df
    new TriangleMesh(newptseq, refMesh.cells)
  }

  override lazy val parent: ShapeModels = scene.models

  val instances = new ShapeModelInstances(this)
  override val children = Seq(instances)

  lazy val landmarkNameGenerator: NameGenerator = NameGenerator.defaultGenerator

  val landmarks = new ArrayBuffer[VirtualLandmark] {
    def add(index: Int) = {
    	val lm = new VirtualLandmark(index)
    	lm.name = landmarkNameGenerator.nextName
    	this += lm
    	publish(ShapeModel.LandmarksAdded(ShapeModel.this))
    }
  }
  
  parent.add(this)

}

class ShapeModelInstances(val model: ShapeModel)(implicit override val scene: Scene) extends SceneTreeObjectContainer[ShapeModelInstance] {
  name = "Instances"
  override lazy val isNameUserModifiable = false
  override lazy val parent = model

  def create(name: String = ""): ShapeModelInstance = {
    val actualName = if (!("".equals(name))) name else "Instance #" + (children.length + 1)
    val child = new ShapeModelInstance(this)
    child.name = actualName
    add(child)
    child
  }
}

object ShapeModelInstance {
  case class CoefficientsChanged(source: ShapeModelInstance) extends Event
}

class ShapeModelInstance(container: ShapeModelInstances) extends ThreeDObject {
  override lazy val parent: ShapeModelInstances = container
  lazy val model = parent.model
  private var _coefficients: IndexedSeq[Float] = IndexedSeq.fill(model.gp.rank)(0.0f)

  val meshRepresentation = new ShapeModelInstanceMeshRepresentation
  def coefficients: IndexedSeq[Float] = { _coefficients }

  def coefficients_=(newCoeffs: IndexedSeq[Float]) = {
    if (_coefficients != newCoeffs) {
      _coefficients = newCoeffs
      meshRepresentation.triangleMesh = model.calculateMesh(_coefficients);
      publish(ShapeModelInstance.CoefficientsChanged(this))
    }
  }
  representations.add(meshRepresentation)

  override val landmarks = new MoveableLandmarks(this)

  class ShapeModelInstanceMeshRepresentation extends Mesh {
    name = "Mesh"
    override lazy val isNameUserModifiable = false
    private var _mesh: TriangleMesh = model.calculateMesh(_coefficients)
    def triangleMesh = { _mesh }
    private[ShapeModelInstance] def triangleMesh_=(newMesh: TriangleMesh) = {
      _mesh = newMesh
      publish(Mesh.GeometryChanged(this))
    }
    override lazy val parent: ShapeModelInstance = ShapeModelInstance.this

    def addLandmarkAt(point: Point3D) = {
      val index = _mesh.findClosestPoint(point)._2
      val model = parent.parent.parent
      model.landmarks.add(index)
    }
  }
}