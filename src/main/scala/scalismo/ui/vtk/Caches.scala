package scalismo.ui.vtk

import scalismo.geometry._3D
import scalismo.image.DiscreteScalarImage
import scalismo.mesh.TriangleMesh
import scalismo.ui.util.Cache
import vtk.{ vtkPolyData, vtkStructuredPoints }

object Caches {
  final val MeshCache = new Cache[TriangleMesh, vtkPolyData]
  final val ImageCache = new Cache[DiscreteScalarImage[_3D, _], vtkStructuredPoints]
  final val ImageIntensityRangeCache = new Cache[vtkStructuredPoints, (Double, Double)]
}
