package org.statismo.stk.ui.vtk

import org.statismo.stk.core.geometry._3D
import org.statismo.stk.core.image.DiscreteScalarImage
import org.statismo.stk.core.mesh.TriangleMesh
import org.statismo.stk.ui.util.Cache
import vtk.{vtkPolyData, vtkStructuredPoints}

object Caches {
  final val MeshCache = new Cache[TriangleMesh, vtkPolyData]
  final val ImageCache = new Cache[DiscreteScalarImage[_3D,_], vtkStructuredPoints]
  final val ImageIntensityRangeCache = new Cache[vtkStructuredPoints, (Double, Double)]
}
