package org.statismo.stk.ui.vtk

import org.statismo.stk.core.mesh.TriangleMesh
import org.statismo.stk.ui.Image3D
import org.statismo.stk.ui.util.Cache
import vtk.{vtkStructuredPoints, vtkPolyData}

object Caches {
  final val MeshCache = new Cache[TriangleMesh, vtkPolyData]
  final val ImageCache = new Cache[Image3D[_], vtkStructuredPoints]
  final val ImageIntensityRangeCache = new Cache[vtkStructuredPoints, (Double, Double)]
}
