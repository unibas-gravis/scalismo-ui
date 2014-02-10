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
import scala.swing.Swing
import scala.concurrent.ExecutionContext.Implicits.global
import scala.async.Async.{ async, await }
import scala.swing.Reactor

class ShapeModels(implicit override val scene: Scene) extends SceneTreeObjectContainer[ShapeModel] with RemoveableChildren {
  override lazy val parent = scene
  name = "Statistical Shape Models"
  override lazy val isNameUserModifiable = false

}

object ShapeModel extends SceneTreeObjectFactory[ShapeModel] with FileIoMetadata {
  override val description = "Statistical Shape Model (.h5)"
  override val fileExtensions = Seq("h5")
  val metadata = this

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
  
  def apply(peer: StatisticalMeshModel, template: Option[ShapeModel])(implicit scene: Scene) = {
    val nm = new ShapeModel(peer)
    if (template.isDefined) {
      nm.name = template.get.name
      template.get.instances.children.foreach { inst =>
        nm.instances.create(inst)
      }
      template.get.landmarks.children.foreach{lm =>
        nm.landmarks.create(lm)
      }
    }
    nm
  }
}

class ShapeModel(val peer: StatisticalMeshModel)(implicit override val scene: Scene) extends SceneTreeObject with Removeable {

  lazy val gp: SpecializedLowRankGaussianProcess[ThreeD] = {
    peer.gp match {
      case specializedGP: SpecializedLowRankGaussianProcess[ThreeD] => specializedGP
      case gp => gp.specializeForPoints(peer.mesh.points.force)
    }
  }

  def calculateMesh(coefficients: IndexedSeq[Float]) = {
    val vector = DenseVector[Float](coefficients.toArray)
    val ptdefs = gp.instanceAtPoints(vector)
    val newptseq = for ((pt, df) <- ptdefs) yield pt + df
    new TriangleMesh(newptseq, peer.mesh.cells)
  }

  override lazy val parent: ShapeModels = scene.models

  val instances = new ShapeModelInstances(this)
  override val children = Seq(instances)

  lazy val landmarkNameGenerator: NameGenerator = NameGenerator.defaultGenerator

  val landmarks = new ReferenceLandmarks(this)
  
  parent.add(this)

}

class ShapeModelInstances(val model: ShapeModel)(implicit override val scene: Scene) extends SceneTreeObjectContainer[ShapeModelInstance] with RemoveableChildren {
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
  
  def create(template: ShapeModelInstance): ShapeModelInstance = {
    val child = new ShapeModelInstance(this)
    child.name = template.name
    //child.coefficients = template.coefficients
    child.landmarks.color = template.landmarks.color
    child.landmarks.radius = template.landmarks.radius
    child.landmarks.name = template.landmarks.name
    child.meshRepresentation.name = template.meshRepresentation.name
    child.meshRepresentation.color = template.meshRepresentation.color
    add(child)
    child
  }
}

object ShapeModelInstance {
  case class CoefficientsChanged(source: ShapeModelInstance) extends Event
}

class ShapeModelInstance(container: ShapeModelInstances) extends ThreeDObject with Removeable {
  override lazy val parent: ShapeModelInstances = container
  lazy val model = parent.model
  private var _coefficients: IndexedSeq[Float] = IndexedSeq.fill(model.gp.rank)(0.0f)

  val meshRepresentation = new ShapeModelInstanceMeshRepresentation
  def coefficients: IndexedSeq[Float] = { _coefficients }

  def coefficients_=(newCoeffs: IndexedSeq[Float]) = {

    if (_coefficients != newCoeffs) {
      _coefficients = newCoeffs
      publish(ShapeModelInstance.CoefficientsChanged(this))
      async {
        meshRepresentation.triangleMesh = model.calculateMesh(newCoeffs);
      }
    }
  }
  representations.add(meshRepresentation)

  override val landmarks = new MoveableLandmarks(this)

  class ShapeModelInstanceMeshRepresentation extends Mesh {
    name = "Mesh"
    override lazy val isNameUserModifiable = false
    override lazy val isCurrentlyRemoveable = false
    private var _mesh: TriangleMesh = model.calculateMesh(_coefficients)
    def triangleMesh = { _mesh }
    private[ShapeModelInstance] def triangleMesh_=(newMesh: TriangleMesh) = {
      _mesh = newMesh
      Swing.onEDT(publish(Mesh.GeometryChanged(this)))
    }
    override lazy val parent: ShapeModelInstance = ShapeModelInstance.this

    def addLandmarkAt(point: Point3D) = {
      parent.landmarks.addAt(point)
    }
  }
}