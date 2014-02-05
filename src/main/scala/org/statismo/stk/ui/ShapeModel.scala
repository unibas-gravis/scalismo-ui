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

case class ShapeModels(implicit override val scene: Scene) extends SceneTreeObjectContainer[ShapeModel] {
  override lazy val parent = scene
  name = "Statistical Shape Models"

  def load(filenames: String*): Seq[ShapeModel] = {
    val loaded = filenames.map({ fn => ShapeModel(fn) }).filter(_.isSuccess).map(_.get)
    loaded
  }
}

object ShapeModel extends Loadable[ShapeModel] {
  override val description = "Statistical Shape Model (.h5)"
  override val fileExtensions = Seq("h5")

  override def apply(file: File)(implicit scene: Scene): Try[ShapeModel] = apply(file, 1)
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
  
  case class LandmarksChanged(source: ShapeModel) extends Event
}

case class ShapeModel(val rawModel: StatisticalMeshModel)(implicit override val scene: Scene) extends SceneTreeObject {

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
  
  
  val landmarks = new ArrayBuffer[Landmark] {
    def addLandmarkAtPointIndex(index: Int) = {
      
    }
  }
  
  parent.add(this)
  
  case class VirtualLandmark(pointIndex: Int) {
    
  }
}


case class ShapeModelInstances(model: ShapeModel)(implicit override val scene: Scene) extends SceneTreeObjectContainer[ShapeModelInstance] {
  name = "Instances"
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

case class ShapeModelInstance(container: ShapeModelInstances) extends ThreeDObject {
  override lazy val parent: ShapeModelInstances = container
  lazy val model = parent.model
  private var _coefficients: IndexedSeq[Float] = IndexedSeq.fill(model.gp.rank)(0.0f)

  private val meshRepresentation = new ShapeModelInstanceMeshRepresentation
  def coefficients: IndexedSeq[Float] = { _coefficients }

  def coefficients_=(newCoeffs: IndexedSeq[Float]) = {
    if (_coefficients != newCoeffs) {
      _coefficients = newCoeffs
      meshRepresentation.triangleMesh = model.calculateMesh(_coefficients);
      publish(ShapeModelInstance.CoefficientsChanged(this))
    }
  }
  representations.add(meshRepresentation)
  
  override val landmarks = {
    new StaticLandmarks(this)
  }

  case class ShapeModelInstanceMeshRepresentation extends Mesh {
    name = "Mesh"
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
      println("want to add a LM at index "+index)
      model.landmarks.addLandmarkAtPointIndex(index)
    }
  }
}