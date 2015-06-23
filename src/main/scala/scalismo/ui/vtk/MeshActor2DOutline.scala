package scalismo.ui.vtk

import _root_.vtk.{ vtkCutter, vtkPlane, vtkPolyData }
import scalismo.geometry.{ Point, _3D }
import scalismo.ui.Mesh.MeshRenderable2DOutline
import scalismo.ui._
import scalismo.utils.MeshConversion

class MeshActor2DOutline(source: MeshRenderable2DOutline)(implicit vtkViewport: VtkViewport) extends PolyDataActor with ClickableActor with ActorLineWidth with ActorColor with ActorOpacity {
  override lazy val color = source.color
  override lazy val opacity = source.opacity
  override lazy val lineWidth = source.lineWidth

  val viewport = vtkViewport.viewport.asInstanceOf[TwoDViewport]
  val scene = viewport.scene

  listenTo(scene)
  private var polyMesh: Option[vtkPolyData] = None
  private var meshOrNone: Option[Mesh] = None

  source.meshOrNone.foreach {
    m =>
      listenTo(m)
      polyMesh = Some(Caches.MeshCache.getOrCreate(m.peer, MeshConversion.meshToVtkPolyData(m.peer, None)))
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
    meshOrNone.foreach {
      mesh =>
        if (withGeometry) {
          polyMesh = Some(Caches.MeshCache.getOrCreate(mesh.peer, MeshConversion.meshToVtkPolyData(mesh.peer, None)))
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
    case Scene.SlicingPosition.PointChanged(s, _, _) => update()
    case Mesh.GeometryChanged(m) => update(withGeometry = true)
    case Mesh.Reloaded(m) => update(withGeometry = true)
    case SceneTreeObject.Destroyed(m) =>
      deafTo(m)
      meshOrNone = None
  }

  override def clicked(point: Point[_3D]) = {
    meshOrNone.foreach {
      m => m.addLandmarkAt(point)
    }
  }

  override def onDestroy() = this.synchronized {
    deafTo(scene)
    meshOrNone.foreach {
      m => deafTo(m)
    }
    super.onDestroy()
    cutEdges.Delete()
    plane.Delete()
  }
}