package org.statismo.stk.ui.vtk

import scala.swing.Component
import vtk.vtkPanel
import javax.swing.JPanel
import java.awt.BorderLayout
import javax.swing.JComponent
import org.statismo.stk.ui.SceneObject
import org.statismo.stk.ui.Surface
import vtk.vtkActor
import scala.collection.immutable.HashMap
import vtk.vtkPolyDataMapper
import vtk.vtkPolyData
import org.statismo.stk.core.mesh.TriangleMesh
import org.statismo.stk.core.utils.MeshConversion
import scala.swing.Reactor
import scala.swing.event.Event
import scala.swing.Publisher

case class RenderRequest extends Event

class VtkPanel extends Component with Reactor {
  lazy val vtk = new vtkPanel
  override lazy val peer = {
    val panel = new JPanel(new BorderLayout())
    panel.add(vtk, BorderLayout.CENTER);
    panel
  }
  
  private var actorsMap = new HashMap[SceneObject, MeshActor]
  
  def setObjects(obj: List[SceneObject]) = {
    var changed = false
    actorsMap.filterNot(obj.contains(_)).foreach(f => {
      actorsMap -= f._1
      deafTo(f._2)
      vtk.GetRenderer.RemoveActor(f._2)
      changed = true
    })
    
    val brandNew = obj.filterNot(actorsMap contains)
    val surfaces: List[Surface] = brandNew.filter(_.isInstanceOf[Surface]).map(_.asInstanceOf[Surface])
    
    surfaces foreach { surface =>
      val actor = new MeshActor(surface)
      listenTo(actor)
      vtk.GetRenderer.AddActor(actor)
      actorsMap += Tuple2(surface, actor)
      changed = true
    }
    if (changed) {
      vtk.GetRenderer.ResetCamera()
      vtk.Render()
    }
  }
  
  reactions += {
    case RenderRequest() => {vtk.Render()}
  }
}

class MeshActor(val surface: Surface) extends vtkActor with Publisher {
  listenTo(surface)
  var mesh: Option[vtkPolyData] = None
  val mapper = new vtkPolyDataMapper
  this.SetMapper(mapper)
  reloadGeometry()
  
  reactions += {
    case SceneObject.GeometryChanged => reloadGeometry()
  }
  
  def reloadGeometry() {
    mesh = Some(MeshConversion.meshToVTKPolyData(surface.mesh, mesh))
    mapper.SetInputData(mesh.get)
    mapper.Modified()
    publish(RenderRequest())
  }
}
