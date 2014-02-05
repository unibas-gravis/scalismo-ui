package org.statismo.stk.ui.vtk

import scala.collection.Seq
import org.statismo.stk.ui.Mesh
import vtk.vtkActor
import vtk.vtkPolyData
import vtk.vtkPolyDataMapper
import org.statismo.stk.core.utils.MeshConversion
import org.statismo.stk.core.geometry.Point3D
import org.statismo.stk.ui.Sphere
import vtk.vtkSphereSource

class SphereActor(source: Sphere) extends PolyDataActor with ColorableActor {
	private lazy val sphere = new vtkSphereSource
	override lazy val colorable = source
	listenTo(source)
	
	mapper.SetInputConnection(sphere.GetOutputPort())
    setGeometry()
	
	reactions += {
	  case Sphere.GeometryChanged(m) => setGeometry
	}
	
	def setGeometry() {
	  sphere.SetCenter(source.center.x, source.center.y, source.center.z)
	  sphere.SetRadius(source.radius)
	  sphere.Modified()
      mapper.Modified()
      publish(VtkContext.RenderRequest(this))
    }
}