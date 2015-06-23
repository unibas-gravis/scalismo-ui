package scalismo.ui

import java.io.File

import breeze.linalg.DenseVector
import scalismo.geometry.{ Point, _3D }
import scalismo.io.StatismoIO
import scalismo.io.StatismoIO.{ CatalogEntry, StatismoModelType }
import scalismo.mesh.TriangleMesh
import scalismo.statisticalmodel.StatisticalMeshModel
import scalismo.ui.ShapeModelInstance.MeshRepresentation
import scalismo.ui.UiFramework.{ SelectionTableModel, TableRow }

import scala.collection.immutable
import scala.swing.event.Event
import scala.util.{ Failure, Success, Try }

class ShapeModels(implicit override val scene: Scene) extends StandaloneSceneTreeObjectContainer[ShapeModel] with RemoveableChildren {
  override lazy val parent = scene
  name = "Statistical Shape Models"
  protected[ui] override lazy val isNameUserModifiable = false

  def createFromFile(file: File, numberOfInstances: Int = 1): Try[ShapeModel] = ShapeModel.createFromFile(file, numberOfInstances)

  def createFromPeer(peer: StatisticalMeshModel, numberOfInstances: Int = 1): ShapeModel = ShapeModel.createFromPeer(peer, numberOfInstances)

  def createFromPeer(peer: StatisticalMeshModel, template: ShapeModel): ShapeModel = ShapeModel.createFromPeer(peer, template)
}

object ShapeModel extends SceneTreeObjectFactory[ShapeModel] with FileIoMetadata {
  protected[ui] override val ioMetadata = this
  override val description = "Statistical Shape Model"
  override val fileExtensions = immutable.Seq("h5")

  protected[ui] override def tryCreate(file: File)(implicit scene: Scene): Try[ShapeModel] = createFromFile(file, 1)

  private def selectPathFromFile(file: File): Try[String] = {
    val catalogTry = StatismoIO.readModelCatalog(file)

    catalogTry match {

      case Failure(ex) =>
        if (ex == StatismoIO.NoCatalogPresentException) {
          // no catalog, assuming a single contained model
          Success("/")
        } else Failure(ex)

      case Success(catalog) =>
        val entries = catalog.filter(e => e.modelType == StatismoModelType.Polygon_Mesh)

        if (entries.isEmpty) {
          Failure(new IllegalArgumentException("File does not contain any usable model"))
        } else if (entries.length == 1) {
          Success(entries.head.modelPath)
        } else {
          val title = "Select shape model to load"
          val description = "The file contains more that one shape model. Please select the one you wish to load."
          val columns = immutable.Seq("Name", "Path")

          class Row(payload: CatalogEntry) extends TableRow[CatalogEntry](payload) {
            override val columnNames: immutable.Seq[String] = columns

            override def columnValue(index: Int) = if (index == 0) payload.name else payload.modelPath
          }

          val rows = entries.map(e => new Row(e)).to[immutable.Seq]
          val table = new SelectionTableModel[CatalogEntry](rows)

          UiFramework.instance.selectFromTable(table, title, description, canMultiSelect = false, canCancel = true)
          val path = table.selected.headOption.map(_.modelPath).orNull
          if (path != null) {
            Success(path)
          } else {
            Failure(CommonExceptions.UserCancelledActionException)
          }
        }
    }
  }

  def createFromFile(file: File, numberOfInstances: Int)(implicit scene: Scene): Try[ShapeModel] = {
    for {
      path <- selectPathFromFile(file)
      raw <- StatismoIO.readStatismoMeshModel(file, modelPath = path)
    } yield {
      val shape = new ShapeModel(raw)
      shape.name = file.getName
      0 until numberOfInstances foreach {
        i => shape.instances.create()
      }
      shape
    }
  }

  def createFromPeer(peer: StatisticalMeshModel, template: ShapeModel)(implicit scene: Scene) = {
    val nm = new ShapeModel(peer)
    template.instances.foreach(nm.instances.create)
    template.landmarks.foreach(nm.landmarks.create)
    nm
  }

  def createFromPeer(peer: StatisticalMeshModel, numberOfInstances: Int, nameOpt: Option[String] = None)(implicit scene: Scene) = {
    val nm = new ShapeModel(peer)
    nameOpt.foreach(n => nm.name = n)
    0 until numberOfInstances foreach {
      i => nm.instances.create()
    }
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

  val landmarks = new ReferenceLandmarks(this)

  def calculateMesh(coefficients: IndexedSeq[Float]) = {
    val vector = DenseVector[Float](coefficients.toArray)
    peer.instance(vector)
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

  class MeshRepresentation(override val parent: ShapeModelInstance) extends Mesh {
    name = "Mesh"
    protected[ui] override lazy val isNameUserModifiable = false
    protected[ui] override lazy val isCurrentlyRemoveable = false
    private var mesh: TriangleMesh = parent.shapeModel.calculateMesh(parent.coefficients)

    def peer = {
      mesh
    }

    private[ShapeModelInstance] def peer_=(newMesh: TriangleMesh) = {
      mesh = newMesh
      publishEdt(Mesh.GeometryChanged(this))
    }

    override def addLandmarkAt(point: Point[_3D], nameOpt: Option[String]) = {
      parent.landmarks.addAt(point, nameOpt, createLandmarkUncertainty(point))
    }
  }

}

class ShapeModelInstance(container: ShapeModelInstances) extends ThreeDObject with Removeable {
  lazy val shapeModel = container.shapeModel
  override lazy val parent = shapeModel
  private var _coefficients: IndexedSeq[Float] = IndexedSeq.fill(shapeModel.peer.gp.rank)(0.0f)

  val meshRepresentation = new MeshRepresentation(this)

  def coefficients: IndexedSeq[Float] = {
    _coefficients
  }

  def coefficients_=(newCoeffs: IndexedSeq[Float]) = {

    if (_coefficients != newCoeffs) {
      _coefficients = newCoeffs
      meshRepresentation.peer = shapeModel.calculateMesh(newCoeffs)
      publishEdt(ShapeModelInstance.CoefficientsChanged(this))
    }
  }

  representations.add(meshRepresentation)

  override val landmarks = new MoveableLandmarks(this)

}