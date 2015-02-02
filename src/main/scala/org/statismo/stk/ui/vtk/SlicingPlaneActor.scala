package org.statismo.stk.ui.vtk

import org.statismo.stk.core.geometry.{Point, Vector, _3D}
import org.statismo.stk.ui.{Axis, BoundingBox, Scene, TwoDViewport}
import vtk._

class SlicingPlaneActor(source: Scene.SlicingPosition, axis: Axis.Value)(implicit vtkViewport: VtkViewport) extends PolyDataActor {
  val scene = source.scene

  override lazy val currentBoundingBox = BoundingBox.None

  listenTo(scene)

  // explicitly call this in subclasses
  //update()

  reactions += {
    case Scene.SlicingPosition.BoundingBoxChanged(s) => update()
    case Scene.SlicingPosition.PointChanged(s, current, previous) => updateWithSlicingPositionChange(current, previous)
    case Scene.SlicingPosition.VisibilityChanged(s) => update()
  }

  def updateWithSlicingPositionChange(current: Point[_3D], previous: Option[Point[_3D]]): Unit = {
    update(previous.map(current - _))
  }

  def onUpdate(points: vtkPoints) = {}

  def update(camMove: Option[Vector[_3D]] = None) = this.synchronized {
    val bb = source.boundingBox
    val p = source.point

    if (!source.visible) {
      // we still need to draw the lines in 2D, but we'll draw them black on black if they're supposed to be invisible.
      GetProperty().SetColor(0, 0, 0)
    } else {
      axis match {
        case Axis.X => GetProperty().SetColor(1, 0, 0)
        case Axis.Y => GetProperty().SetColor(0, 1, 0)
        case Axis.Z => GetProperty().SetColor(0, 0, 1)
      }
    }

    val points = new vtkPoints()

    axis match {
      case Axis.X =>
        points.InsertNextPoint(p(0), bb.yMin, bb.zMin)
        points.InsertNextPoint(p(0), bb.yMax, bb.zMin)
        points.InsertNextPoint(p(0), bb.yMax, bb.zMax)
        points.InsertNextPoint(p(0), bb.yMin, bb.zMax)
      case Axis.Y =>
        points.InsertNextPoint(bb.xMin, p(1), bb.zMin)
        points.InsertNextPoint(bb.xMax, p(1), bb.zMin)
        points.InsertNextPoint(bb.xMax, p(1), bb.zMax)
        points.InsertNextPoint(bb.xMin, p(1), bb.zMax)
      case Axis.Z =>
        points.InsertNextPoint(bb.xMin, bb.yMin, p(2))
        points.InsertNextPoint(bb.xMax, bb.yMin, p(2))
        points.InsertNextPoint(bb.xMax, bb.yMax, p(2))
        points.InsertNextPoint(bb.xMin, bb.yMax, p(2))
    }

    onUpdate(points)

    mapper.RemoveAllInputs()

    val poly = new vtk.vtkPolyData()
    poly.SetPoints(points)

    val outline = new vtk.vtkOutlineFilter()
    outline.SetInputData(poly)
    mapper.SetInputConnection(outline.GetOutputPort())
    mapper.Modified()
    outline.Delete()

    poly.Delete()
    points.Delete()

    val needEvent = camMove.map { m =>
      val diff = axis match {
        case Axis.X => m(0)
        case Axis.Y => m(1)
        case Axis.Z => m(2)
      }
      if (diff != 0) {
        publishEdt(VtkContext.MoveCameraRequest(this, axis, diff))
        false
      } else true
    }.getOrElse(true)

    if (needEvent) publishEdt(VtkContext.RenderRequest(this))
  }

  override def onDestroy() = this.synchronized {
    deafTo(scene)
    super.onDestroy()
  }
}

class SlicingPlaneActor3D(plane: Scene.SlicingPosition.SlicingPlaneRenderable3D)(implicit vtkViewport: VtkViewport) extends SlicingPlaneActor(plane.source, plane.axis) {

  val planeActor = new vtkActor
  val planeMapper = new vtkPolyDataMapper
  planeActor.SetMapper(planeMapper)

  reactions += {
    case Scene.SlicingPosition.OpacityChanged(s) => update()
  }

  update()

  override lazy val vtkActors: Seq[vtkActor] = Seq(this, planeActor)

  override def updateWithSlicingPositionChange(current: Point[_3D], previous: Option[Point[_3D]]): Unit = {
    // in 3D, we don't want to change the camera position
    update(None)
  }

  override def onUpdate(points: vtkPoints) = {
    val poly = new vtk.vtkPolyData()
    poly.SetPoints(points)

    val quad = new vtkQuad()
    for (i <- 0 to 3) {
      quad.GetPointIds().SetId(i, i)
    }
    val quads = new vtkCellArray()
    quads.InsertNextCell(quad)
    poly.SetPolys(quads)
    planeMapper.SetInputData(poly)
    planeMapper.Modified()
    quads.Delete()
    quad.Delete()
    poly.Delete()

    planeActor.GetProperty().SetColor(this.GetProperty().GetColor())
    planeActor.GetProperty().SetOpacity(scene.slicingPosition.opacity)
  }

  override def onDestroy() = this.synchronized {
    super.onDestroy()
    planeMapper.Delete()
  }
}

class SlicingPlaneActor2D(plane: Scene.SlicingPosition.SlicingPlaneRenderable2D)(implicit vtkViewport: VtkViewport) extends SlicingPlaneActor(plane.source, vtkViewport.viewport.asInstanceOf[TwoDViewport].axis) {
  update()
}
