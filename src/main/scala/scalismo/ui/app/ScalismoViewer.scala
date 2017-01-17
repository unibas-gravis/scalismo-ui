/*
 * Copyright (C) 2016  University of Basel, Graphics and Vision Research Group 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scalismo.ui.app

import java.io.{IOException, File}

import scalismo.geometry._3D
import scalismo.io.{ StatismoIO, LandmarkIO, ImageIO, MeshIO }
import scalismo.ui.api.{SimplePluginAPI, ShapeModelTransformationView, ScalismoUI}
import scalismo.ui.model.StatusMessage
import scalismo.ui.view.dialog.ErrorDialog

import scala.util.{ Failure, Success }

/**
 * scalismo Viewer is a simple viewer to visualize 3D Shape Models, images, meshes, ...
 */
object ScalismoViewer {


  def showErrorMessage(file : File, exception: Throwable) : Unit = {
    val message = s"Unable to load file ${file.getName}"
    System.err.println(message)
    System.err.println(exception.getMessage)
  }

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
            case Failure(t) => {showErrorMessage(new File(arg), t)}
          }
        }
        case "stl" => {
          MeshIO.readMesh(new File(arg)) match {
            case Success(mesh) => ui.show(defaultGroup, mesh, arg)
            case Failure(t) => {showErrorMessage(new File(arg), t)}
          }
        }
        case "vtk" => {
          MeshIO.readMesh(new File(arg)) match {
            case Success(mesh) => ui.show(defaultGroup, mesh, arg)
            case Failure(_) => {
              ImageIO.read3DScalarImageAsType[Float](new File(arg), resampleOblique = true) match {
                case Success(image) => ui.show(defaultGroup, image, arg)
                case Failure(t) => {showErrorMessage(new File(arg), t)}
              }
            }
          }
        }
        case "nii" => {
          ImageIO.read3DScalarImageAsType[Float](new File(arg)) match {
            case Success(image) => ui.show(defaultGroup, image, arg)
            case Failure(t) => {showErrorMessage(new File(arg), t)}
          }
        }
        case "json" => {
          LandmarkIO.readLandmarksJson[_3D](new File(arg)) match {
            case Success(lms) => ui.show(defaultGroup, lms, arg)
            case Failure(t) => {showErrorMessage(new File(arg), t)}
          }
        }
        case "csv" => {
          LandmarkIO.readLandmarksCsv[_3D](new File(arg)) match {
            case Success(lms) => ui.show(defaultGroup, lms, arg)
            case Failure(t) => {showErrorMessage(new File(arg), t)}
          }
        }
        case _ => {
          showErrorMessage(new File(arg), new IOException("Unknown file extension " +extension))
        }
      }
    }
  }

}
