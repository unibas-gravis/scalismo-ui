package scalismo.ui

import java.io.File

import breeze.linalg.DenseVector
import scalismo.geometry.{ Point, _3D }
import scalismo.io.StatismoIO
import scalismo.io.StatismoIO.{ CatalogEntry, StatismoModelType }
import scalismo.mesh.TriangleMesh
import scalismo.statisticalmodel.StatisticalMeshModel
import scalismo.ui.ShapeModelInstance.MeshViewRepresentation
import scalismo.ui.UiFramework.{ SelectionTableModel, TableRow }

import scala.collection.immutable
import scala.swing.event.Event
import scala.util.{ Failure, Success, Try }

class ShapeModels(implicit override val scene: Scene) extends StandaloneSceneTreeObjectContainer[ShapeModelView] with RemoveableChildren {
  override lazy val parent = scene
  name = "Statistical Shape Models"
  protected[ui] override lazy val isNameUserModifiable = false

  def createFromFile(file: File, numberOfInstances: Int = 1): Try[ShapeModelView] = ShapeModelView.createFromFile(file, numberOfInstances)

  def createFromSource(source: StatisticalMeshModel, numberOfInstances: Int = 1): ShapeModelView = ShapeModelView.createFromSource(source, numberOfInstances)

  def createFromSource(source: StatisticalMeshModel, template: ShapeModelView): ShapeModelView = ShapeModelView.createFromSource(source, template)
}

object ShapeModelView extends SceneTreeObjectFactory[ShapeModelView] with FileIoMetadata {
  protected[ui] override val ioMetadata = this
  override val description = "Statistical Shape Model"
  override val fileExtensions = immutable.Seq("h5")

  protected[ui] override def tryCreate(file: File)(implicit scene: Scene): Try[ShapeModelView] = createFromFile(file, 1)

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

  def createFromFile(file: File, numberOfInstances: Int)(implicit scene: Scene): Try[ShapeModelView] = {
    for {
      path <- selectPathFromFile(file)
      raw <- StatismoIO.readStatismoMeshModel(file, modelPath = path)
    } yield {
      val shape = new ShapeModelView(raw)
      shape.name = file.getName
      0 until numberOfInstances foreach {
        i => shape.instances.create()
      }
      shape
    }
  }

  def createFromSource(source: StatisticalMeshModel, template: ShapeModelView)(implicit scene: Scene): ShapeModelView = {
    val nm = new ShapeModelView(source)
    template.instances.foreach(nm.instances.create)
    template.landmarks.foreach(nm.landmarks.create)
    nm
  }

  def createFromSource(source: StatisticalMeshModel, numberOfInstances: Int, nameOpt: Option[String] = None)(implicit scene: Scene): ShapeModelView = {
    val nm = new ShapeModelView(source)
    nameOpt.foreach(n => nm.name = n)
    0 until numberOfInstances foreach {
      i => nm.instances.create()
    }
    nm
  }

}

class ShapeModelView protected[ui] (override val source: StatisticalMeshModel)(implicit override val scene: Scene) extends UIView[StatisticalMeshModel] with SceneTreeObject with Saveable with Removeable {
  override lazy val parent: ShapeModels = scene.shapeModels

  override lazy val saveableMetadata = ShapeModelView

  override def saveToFile(file: File): Try[Unit] = {
    StatismoIO.writeStatismoMeshModel(source, file)
  }

  val instances = new ShapeModelInstances(this)

  protected[ui] override def children = instances.children

  val landmarks = new ReferenceLandmarks(this)

  def calculateMesh(coefficients: IndexedSeq[Float]) = {
    val vector = DenseVector[Float](coefficients.toArray)
    source.instance(vector)
  }

  parent.add(this)
}

class ShapeModelInstances(val shapeModel: ShapeModelView)(implicit val scene: Scene) extends SceneTreeObjectContainer[ShapeModelInstance] with RemoveableChildren {
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

  class MeshViewRepresentation(override val parent: ShapeModelInstance) extends MeshView {
    name = "Mesh"
    protected[ui] override lazy val isNameUserModifiable = false
    protected[ui] override lazy val isCurrentlyRemoveable = false
    private var mesh: TriangleMesh = recalculateMesh()

    override def source = mesh

    listenTo(parent)

    reactions += {
      case ShapeModelInstance.CoefficientsChanged(_) =>
        mesh = recalculateMesh()
        publishEdt(MeshView.GeometryChanged(this))
      case RigidlyTransformable.RigidTransformationChanged(_) =>
        mesh = recalculateMesh()
        publishEdt(MeshView.GeometryChanged(this))
    }

    private def recalculateMesh(): TriangleMesh = {
      val m = parent.shapeModel.calculateMesh(parent.coefficients)
      parent.rigidTransformation match {
        case None => m
        case Some(t) => m.transform(t)
      }
    }

    override def addLandmarkAt(point: Point[_3D], nameOpt: Option[String]) = {
      parent.landmarks.addAt(point, nameOpt, createLandmarkUncertainty(point))
    }
  }

}

class ShapeModelInstance(container: ShapeModelInstances) extends ThreeDObject with RigidlyTransformable {
  lazy val shapeModel = container.shapeModel
  override lazy val parent = shapeModel
  private var _coefficients: IndexedSeq[Float] = IndexedSeq.fill(shapeModel.source.gp.rank)(0.0f)

  val meshRepresentation = new MeshViewRepresentation(this)

  def coefficients: IndexedSeq[Float] = {
    _coefficients
  }

  def coefficients_=(newCoeffs: IndexedSeq[Float]) = {

    if (_coefficients != newCoeffs) {
      _coefficients = newCoeffs
      publishEdt(ShapeModelInstance.CoefficientsChanged(this))
    }
  }

  representations.add(meshRepresentation)

  override val landmarks = new MoveableLandmarks(this)

}
