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

import java.io.{File, IOException}
import scalismo.geometry._3D
import scalismo.io.{ImageIO, LandmarkIO, MeshIO, StatismoIO, StatisticalModelIO}
import scalismo.ui.api.ScalismoUI
import scalismo.ui.util.FileUtil
import vtkjava.InitializationMode

import scala.util.{Failure, Success}

/**
 * scalismo Viewer is a simple viewer to visualize 3D Shape Models, images, meshes, ...
 */
object ScalismoViewer {

  def showErrorMessage(file: File, exception: Throwable): Unit = {
    val message = s"Unable to load file ${file.getName}"
    System.err.println(message)
    System.err.println(exception.getMessage)
  }

  def main(args: Array[String]): Unit = {
    vtkjava.VtkNativeLibraries.initialize(InitializationMode.WARN_VERBOSE)
    //scalismo.initialize()


    val ui = ScalismoUI("Scalismo Viewer")

    val defaultGroup = ui.createGroup("Default Group")

    for (filename <- args) {
      val file = new File(filename)
      if (!file.isFile) {
        showErrorMessage(file, new IOException(s"name does not identify a valid file."))
      } else {

        val basename = FileUtil.basename(file)
        val extension = FileUtil.extension(file).toLowerCase

        extension match {
          case "h5" =>
            StatisticalModelIO.readStatisticalMeshModel(new java.io.File(filename)) match {
              case Success(model) =>
                // we create for every model a new group
                val modelGroup = ui.createGroup(basename)
                ui.show(modelGroup, model, basename)
              case Failure(t) => showErrorMessage(file, t)
            }

          case "stl" =>
            MeshIO.readMesh(file) match {
              case Success(mesh) => ui.show(defaultGroup, mesh, basename)
              case Failure(t)    => showErrorMessage(file, t)
            }

          case "vtk" =>
            MeshIO.readMesh(file) match {
              case Success(mesh) => ui.show(defaultGroup, mesh, basename)
              case Failure(_) =>
                ImageIO.read3DScalarImageAsType[Float](file) match {
                  case Success(image) => ui.show(defaultGroup, image, basename)
                  case Failure(t)     => showErrorMessage(file, t)
                }
            }

          case "nii" =>
            ImageIO.read3DScalarImageAsType[Float](file) match {
              case Success(image) => ui.show(defaultGroup, image, basename)
              case Failure(t)     => showErrorMessage(file, t)
            }

          case "json" =>
            LandmarkIO.readLandmarksJson[_3D](file) match {
              case Success(lms) => ui.show(defaultGroup, lms, basename)
              case Failure(t)   => showErrorMessage(file, t)
            }

          case "csv" =>
            LandmarkIO.readLandmarksCsv[_3D](file) match {
              case Success(lms) => ui.show(defaultGroup, lms, basename)
              case Failure(t)   => showErrorMessage(file, t)
            }

          case _ =>
            showErrorMessage(file, new IOException("Unknown file extension: " + extension))
        }
      }
    }
  }
}
