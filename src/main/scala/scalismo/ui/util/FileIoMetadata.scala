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

package scalismo.ui.util

/**
 * Metadata for File I/O.
 *
 * This trait is used to provide the correct
 * information to UI elements like file choosers.
 */
trait FileIoMetadata {

  /** The supported file extensions (no leading dots) */
  def fileExtensions: List[String]

  /** Human-readable description */
  def description: String

  def longDescription: String = {
    description + fileExtensions.mkString(" (*.", ", *.", ")")
  }
}

object FileIoMetadata {

  val Png: FileIoMetadata = new FileIoMetadata {
    override val description = "PNG Image"
    override val fileExtensions = List("png")
  }

  val TriangleMesh: FileIoMetadata = new FileIoMetadata {
    override val description = "Triangle Mesh"
    override val fileExtensions = List("stl", "vtk", "ply")
  }

  val ScalarMeshField: FileIoMetadata = new FileIoMetadata {
    override val description = "Scalar Mesh Field"
    override val fileExtensions = List("vtk")
  }

  val VertexColorMesh: FileIoMetadata = new FileIoMetadata {

    override def fileExtensions: List[String] = List("ply")

    override def description: String = "Triangle Mesh with vertex color"
  }

  val TetrahedralMeshRead: FileIoMetadata = new FileIoMetadata {

    override def fileExtensions: List[String] = List("vtk", "vtu", "inp")

    override def description: String = "Tetrahedral Mesh"
  }

  val TetrahedralMeshWrite: FileIoMetadata = new FileIoMetadata {

    override def fileExtensions: List[String] = List("vtk", "vtu")

    override def description: String = "Tetrahedral Mesh"
  }

  val ScalarTetrahedralMeshField: FileIoMetadata = new FileIoMetadata {

    override def fileExtensions: List[String] = List("vtk", "vtu")

    override def description: String = "Scalar Tetrahedral Mesh Field"
  }

  val StatisticalVolumeMeshModel: FileIoMetadata = new FileIoMetadata {
    override val description = "Statistical Volume Shape Model"
    override val fileExtensions = List("h5", "json")
  }

  val Image: FileIoMetadata = new FileIoMetadata {
    override val description = "3D Image"
    override val fileExtensions = List("nii", "vtk")
  }

  val Landmarks: FileIoMetadata = new FileIoMetadata {
    override val description = "Landmarks"
    override val fileExtensions = List("json", "csv")
  }

  val StatisticalShapeModel: FileIoMetadata = new FileIoMetadata {
    override val description = "Statistical Shape Model"
    override val fileExtensions = List("h5", "json")
  }

}
