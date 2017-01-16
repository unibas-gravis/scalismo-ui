package scalismo.ui.app

import java.io.File

import scalismo.geometry._3D
import scalismo.io.{ StatismoIO, LandmarkIO, ImageIO, MeshIO }
import scalismo.ui.api.{ ShapeModelTransformationView, ScalismoUI }

import scala.util.{ Failure, Success }

/**
 * scalismo Viewer is a simple viewer to visualize 3D Shape Models, images, meshes, ...
 */
object ScalismoViewer {

  def main(args: Array[String]): Unit = {
    scalismo.initialize()

    val ui = ScalismoUI()

    val defaultGroup = ui.createGroup("default")
    for (arg <- args) {
      val extension = arg.split('.').last
      extension match {
        case "h5" => {
          StatismoIO.readStatismoMeshModel(new File(arg)) match {
            case Success(model) => {
              // we create for every model a new group
              val numModels = ui.filter[ShapeModelTransformationView]((_: ShapeModelTransformationView) => true).size
              val modelGroup = ui.createGroup(s"statistical-model-${numModels}")
              ui.show(modelGroup, model, arg)
            }
            case Failure(_) => {}
          }
        }
        case "stl" => {
          MeshIO.readMesh(new File(arg)) match {
            case Success(mesh) => ui.show(defaultGroup, mesh, arg)
            case Failure(_) => {}
          }
        }
        case "vtk" => {
          MeshIO.readMesh(new File(arg)) match {
            case Success(mesh) => ui.show(defaultGroup, mesh, arg)
            case Failure(_) => {
              ImageIO.read3DScalarImageAsType[Float](new File(arg)) match {
                case Success(image) => ui.show(defaultGroup, image, arg)
                case _ => {}
              }
            }
          }
        }
        case "nii" => {
          ImageIO.read3DScalarImageAsType[Float](new File(arg)) match {
            case Success(image) => ui.show(defaultGroup, image, arg)
            case Failure(_) => {}
          }
        }
        case "json" => {
          LandmarkIO.readLandmarksJson[_3D](new File(arg)) match {
            case Success(lms) => ui.show(defaultGroup, lms, arg)
            case Failure(_) => {}
          }
        }
        case "csv" => {
          LandmarkIO.readLandmarksCsv[_3D](new File(arg)) match {
            case Success(lms) => ui.show(defaultGroup, lms, arg)
            case Failure(_) => {}
          }
        }
        case _ => {}
      }
    }
  }

}
