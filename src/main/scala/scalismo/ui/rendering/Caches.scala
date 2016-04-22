package scalismo.ui.rendering

import scalismo.common.DiscreteScalarField
import scalismo.geometry._3D
import scalismo.image.DiscreteScalarImage
import scalismo.mesh.{TriangleMesh3D, ScalarMeshField, TriangleMesh}
import scalismo.ui.util.Cache
import vtk.{ vtkPolyData, vtkStructuredPoints }

object Caches {

  /*
  It turns out that the triangleMesh spends quite a lot of time computing the hash code. As we know here
  that we are only interested in the geometry, we can speed up computation a lot by caching ourselfs
   */
  case class FastCachingTriangleMesh(tm : TriangleMesh3D) {

      override lazy val hashCode : Int = (31 + tm.pointSet.hashCode()) * (31 + tm.triangulation.hashCode())
  }

  final val TriangleMeshCache = new Cache[FastCachingTriangleMesh, vtkPolyData]
  final val ImageCache = new Cache[DiscreteScalarImage[_3D, _], vtkStructuredPoints]
  final val ScalarMeshFieldCache = new Cache[ScalarMeshField[Float], vtkPolyData]
  final val ScalarFieldCache = new Cache[DiscreteScalarField[_3D, Float], vtkPolyData]
}
