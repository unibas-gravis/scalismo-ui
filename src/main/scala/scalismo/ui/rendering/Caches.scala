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

package scalismo.ui.rendering

import scalismo.common.{DiscreteDomain, DiscreteScalarField}
import scalismo.geometry._3D
import scalismo.image.DiscreteScalarImage
import scalismo.mesh.{ScalarMeshField, TriangleMesh3D, VertexColorMesh3D}
import scalismo.ui.util.Cache
import vtk.{vtkPolyData, vtkStructuredPoints}

object Caches {

  /*
  It turns out that the triangleMesh spends quite a lot of time computing the hash code. As we know here
  that we are only interested in the geometry, we can speed up computation a lot by caching ourselfs
   */
  case class FastCachingTriangleMesh(tm: TriangleMesh3D) {

    override lazy val hashCode: Int = (31 + tm.pointSet.hashCode()) * (31 + tm.triangulation.hashCode())
  }

  case class FastCachingVertexColorMesh(mesh : VertexColorMesh3D) {
    override lazy val hashCode: Int =
      (31 + mesh.shape.pointSet.hashCode()) * (31 + mesh.shape.triangulation.hashCode() * 31 + mesh.color.hashCode())
  }

  final val TriangleMeshCache = new Cache[FastCachingTriangleMesh, vtkPolyData]
  final val ImageCache = new Cache[DiscreteScalarImage[_3D, _], vtkStructuredPoints]
  final val ScalarMeshFieldCache = new Cache[ScalarMeshField[Float], vtkPolyData]
  final val ScalarFieldCache = new Cache[DiscreteScalarField[_3D, DiscreteDomain[_3D], Float], vtkPolyData]
  final val VertexColorMeshCache = new Cache[FastCachingVertexColorMesh, vtkPolyData]

}
