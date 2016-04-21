package scalismo.ui.rendering

import scalismo.common.DiscreteScalarField
import scalismo.geometry._3D
import scalismo.image.DiscreteScalarImage
import scalismo.mesh.{ ScalarMeshField, TriangleMesh }
import scalismo.ui.util.Cache
import vtk.{ vtkPolyData, vtkStructuredPoints }

object Caches {
  final val TriangleMeshCache = new Cache[TriangleMesh[_3D], vtkPolyData]
  final val ImageCache = new Cache[DiscreteScalarImage[_3D, _], vtkStructuredPoints]
  final val ScalarMeshFieldCache = new Cache[ScalarMeshField[Float], vtkPolyData]
  final val ScalarFieldCache = new Cache[DiscreteScalarField[_3D, Float], vtkPolyData]
}
