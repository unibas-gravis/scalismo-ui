package org.statismo.stk.ui

import java.io.File

import scala.swing.event.Event
import scala.util.Try
import scala.collection.immutable

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
  protected[ui] override lazy val isNameUserModifiable = false

  def createFromFile(file: File, numberOfInstances: Int = 1) : Try[ShapeModel] = ShapeModel.createFromFile(file, numberOfInstances)
  def createFromPeer(peer: StatisticalMeshModel, numberOfInstances: Int = 1) : ShapeModel = ShapeModel.createFromPeer(peer, numberOfInstances)
  def createFromPeer(peer: StatisticalMeshModel, template: ShapeModel) : ShapeModel = ShapeModel.createFromPeer(peer, template)
}

object ShapeModel extends SceneTreeObjectFactory[ShapeModel] with FileIoMetadata {
  protected[ui] override val ioMetadata = this
  override val description = "Statistical Shape Model"
  override val fileExtensions = immutable.Seq("h5")

  protected[ui] override def tryCreate(file: File)(implicit scene: Scene): Try[ShapeModel] = createFromFile(file, 1)

  def createFromFile(file: File, numberOfInstances: Int)(implicit scene: Scene): Try[ShapeModel] = {
    for {
      raw <- StatismoIO.readStatismoMeshModel(file)
    } yield {
      val shape = new ShapeModel(raw)
      shape.name = file.getName
      0 until numberOfInstances foreach {i => shape.instances.create()}
      shape
    }
  }

  def createFromPeer(peer: StatisticalMeshModel, template: ShapeModel)(implicit scene: Scene) = {
    val nm = new ShapeModel(peer)
    template.instances.foreach(nm.instances.create)
    template.landmarks.foreach(nm.landmarks.create)
    nm
  }

  def createFromPeer(peer: StatisticalMeshModel, numberOfInstances: Int)(implicit scene: Scene) = {
    val nm = new ShapeModel(peer)
    0 until numberOfInstances foreach {i => nm.instances.create()}
    nm
  }

}

class ShapeModel protected[ui] (val peer: StatisticalMeshModel)(implicit override val scene: Scene) extends SceneTreeObject with Saveable with Removeable {
  override lazy val parent: ShapeModels = scene.shapeModels

  override lazy val saveableMetadata = ShapeModel

  override def saveToFile(file: File): Try[Unit] = {
    StatismoIO.writeStatismoMeshModel(peer, file)
  }

  val instances = new ShapeModelInstances(this)

  protected[ui] override def children = instances.children

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
    child.landmarks.name = template.landmarks.name
    child.meshRepresentation.name = template.meshRepresentation.name
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
    protected[ui] override lazy val isNameUserModifiable = false
    protected[ui] override lazy val isCurrentlyRemoveable = false
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