package org.statismo.stk.ui

import java.io.File

import scala.swing.event.Event
import scala.util.Try
import scala.collection.immutable.{HashMap, IndexedSeq}

import org.statismo.stk.core.geometry.Point3D
import org.statismo.stk.core.geometry.ThreeD
import org.statismo.stk.core.io.StatismoIO
import org.statismo.stk.core.mesh.TriangleMesh
import org.statismo.stk.core.statisticalmodel.SpecializedLowRankGaussianProcess
import org.statismo.stk.core.statisticalmodel.StatisticalMeshModel

import breeze.linalg.DenseVector

class ShapeModels(implicit override val scene: Scene) extends StandaloneSceneTreeObjectContainer[ShapeModel] with RemoveableChildren {
  override lazy val parent = scene
  name = "Statistical Shape Models"
  override lazy val isNameUserModifiable = false
}

object ShapeModel extends SceneTreeObjectFactory[ShapeModel] with FileIoMetadata {
  override val ioMetadata = this
  override val description = "Statistical Shape Model"
  override val fileExtensions = Seq("h5")

  override def apply(file: File)(implicit scene: Scene): Try[ShapeModel] = apply(file, 1)

  def apply(filename: String, numberOfInstancesToCreate: Int = 1)(implicit scene: Scene): Try[ShapeModel] = apply(new File(filename), numberOfInstancesToCreate)

  def apply(file: File, numberOfInstancesToCreate: Int)(implicit scene: Scene): Try[ShapeModel] = {
    for {
      raw <- StatismoIO.readStatismoMeshModel(file)
    } yield {
      val shape = new ShapeModel(raw)
      shape.name = file.getName
      0 until numberOfInstancesToCreate foreach {
        i => shape.instances.create()
      }
      shape
    }
  }

  def apply(peer: StatisticalMeshModel, template: Option[ShapeModel])(implicit scene: Scene) = {
    val nm = new ShapeModel(peer)
    if (template.isDefined) {
      nm.name = template.get.name
      template.get.instances.children.foreach {
        inst =>
          nm.instances.create(inst)
      }
      template.get.landmarks.children.foreach {
        lm =>
          nm.landmarks.create(lm)
      }
    }
    nm
  }
}

class ShapeModel(val peer: StatisticalMeshModel)(implicit override val scene: Scene) extends SceneTreeObject with Saveable with Removeable {
  override lazy val parent: ShapeModels = scene.shapeModels

  override lazy val saveableMetadata = ShapeModel

  override def saveToFile(file: File): Try[Unit] = {
    StatismoIO.writeStatismoMeshModel(peer, file)
  }

  val instances = new ShapeModelInstances(this)

  override def children = instances.children

  lazy val landmarkNameGenerator: NameGenerator = NameGenerator.defaultGenerator
  val landmarks = new ReferenceLandmarks(this)

  lazy val gaussianProcess: SpecializedLowRankGaussianProcess[ThreeD] = {
    peer.gp match {
      case specializedGP: SpecializedLowRankGaussianProcess[ThreeD] => specializedGP
      case gp => gp.specializeForPoints(peer.mesh.points.force)
    }
  }

  def calculateMesh(coefficients: IndexedSeq[Float]) = {
    val vector = DenseVector[Float](coefficients.toArray)
    val ptdefs = gaussianProcess.instanceAtPoints(vector).toMap

    peer.mesh.warp({p => p + ptdefs(p)})
  }

  parent.add(this)
}

class ShapeModelInstances(val shapeModel: ShapeModel)(implicit val scene: Scene) extends SceneTreeObjectContainer[ShapeModelInstance] with RemoveableChildren {
  override lazy val publisher = shapeModel

  def create(name: Option[String] = None): ShapeModelInstance = {
    val child = new ShapeModelInstance(this)
    child.name = name.getOrElse("Instance " + (children.length + 1))
    add(child)
    child
  }

  def create(template: ShapeModelInstance): ShapeModelInstance = {
    val child = new ShapeModelInstance(this)
    child.name = template.name
    if (child.coefficients.length == template.coefficients.length) {
      child.coefficients = template.coefficients
    }
    child.landmarks.color = template.landmarks.color
    child.landmarks.radius = template.landmarks.radius
    child.landmarks.name = template.landmarks.name
    child.meshRepresentation.name = template.meshRepresentation.name
    child.meshRepresentation.color = template.meshRepresentation.color
    child.meshRepresentation.opacity = template.meshRepresentation.opacity
    add(child)
    child
  }
}

object ShapeModelInstance {

  case class CoefficientsChanged(source: ShapeModelInstance) extends Event

}

class ShapeModelInstance(container: ShapeModelInstances) extends ThreeDObject with Removeable {
  lazy val shapeModel = container.shapeModel
  override lazy val parent = shapeModel
  private var _coefficients: IndexedSeq[Float] = IndexedSeq.fill(shapeModel.gaussianProcess.rank)(0.0f)

  val meshRepresentation = new ShapeModelInstanceMeshRepresentation

  def coefficients: IndexedSeq[Float] = {
    _coefficients
  }

  def coefficients_=(newCoeffs: IndexedSeq[Float]) = {

    if (_coefficients != newCoeffs) {
      _coefficients = newCoeffs
      meshRepresentation.peer = shapeModel.calculateMesh(newCoeffs)
      publish(ShapeModelInstance.CoefficientsChanged(this))
    }
  }

  representations.add(meshRepresentation)

  override val landmarks = new MoveableLandmarks(this)

  class ShapeModelInstanceMeshRepresentation extends Mesh {
    name = "Mesh"
    override lazy val isNameUserModifiable = false
    override lazy val isCurrentlyRemoveable = false
    private var _mesh: TriangleMesh = shapeModel.calculateMesh(_coefficients)

    def peer = {
      _mesh
    }

    private[ShapeModelInstance] def peer_=(newMesh: TriangleMesh) = {
      _mesh = newMesh
      publish(Mesh.GeometryChanged(this))
    }

    override lazy val parent = ShapeModelInstance.this

    def addLandmarkAt(point: Point3D) = {
      parent.landmarks.addAt(point)
    }
  }

}