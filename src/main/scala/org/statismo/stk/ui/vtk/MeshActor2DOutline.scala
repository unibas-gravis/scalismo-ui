package org.statismo.stk.ui.vtk

import _root_.vtk.{vtkCutter, vtkPlane, vtkPolyData}
import org.statismo.stk.core.geometry.{_3D, Point}
import org.statismo.stk.core.utils.MeshConversion
import org.statismo.stk.ui.Mesh.MeshRenderable2DOutline
import org.statismo.stk.ui._

class MeshActor2DOutline(source: MeshRenderable2DOutline)(implicit vtkViewport: VtkViewport) extends PolyDataActor with LineActor with ClickableActor {
  override lazy val color = source.color
  override lazy val opacity = source.opacity
  override lazy val lineThickness = source.lineThickness

  val viewport = vtkViewport.viewport.asInstanceOf[TwoDViewport]
  val scene = viewport.scene

  listenTo(scene)
  private var polyMesh: Option[vtkPolyData] = None
  private var meshOrNone: Option[Mesh] = None

  source.meshOrNone.map {
    m =>
      listenTo(m)
      polyMesh = Some(Caches.MeshCache.getOrCreate(m.peer, MeshConversion.meshToVTKPolyData(m.peer, None)))
      meshOrNone = Some(m)
  }

  val plane = new vtkPlane()
  plane.SetOrigin(0, 0, 0)
  viewport.axis match {
    case Axis.X => plane.SetNormal(1, 0, 0)
    case Axis.Y => plane.SetNormal(0, 1, 0)
    case Axis.Z => plane.SetNormal(0, 0, 1)
  }

  val cutEdges = new vtkCutter()
  cutEdges.SetInputData(polyMesh.get)
  cutEdges.SetCutFunction(plane)
  cutEdges.SetValue(0, 0)

  mapper.SetInputConnection(cutEdges.GetOutputPort())

  def update(withGeometry: Boolean = false) = {
    meshOrNone.map {
      mesh =>
        if (withGeometry) {
          polyMesh = Some(Caches.MeshCache.getOrCreate(mesh.peer, MeshConversion.meshToVTKPolyData(mesh.peer, None)))
          cutEdges.SetInputData(polyMesh.get)
        }
        cutEdges.SetValue(0, viewport.axis match {
          case Axis.X => scene.slicingPosition.x
          case Axis.Y => scene.slicingPosition.y
          case Axis.Z => scene.slicingPosition.z
        })
        cutEdges.Update()
        //publishEdt(VtkContext.ResetCameraRequest(this))
        publishEdt(VtkContext.RenderRequest(this))
    }
  }

  this.GetProperty().SetInterpolationToGouraud()
  update()

  reactions += {
    case Scene.SlicingPosition.PointChanged(s,_,_) => update()
    case Mesh.GeometryChanged(m) => update(withGeometry = true)
    case Mesh.Reloaded(m) => update(withGeometry = true)
    case SceneTreeObject.Destroyed(m) =>
      deafTo(m)
      meshOrNone = None
  }

  override def clicked(point: Point[_3D]) = {
    meshOrNone.map {
      m => m.addLandmarkAt(point)
    }
  }

  override def onDestroy() = this.synchronized {
    deafTo(scene)
    meshOrNone.map {
      m => deafTo(m)
    }
    super.onDestroy()
    cutEdges.Delete()
    plane.Delete()
  }
}