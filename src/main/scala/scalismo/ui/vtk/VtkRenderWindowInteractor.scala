package scalismo.ui.vtk

import java.awt.event.{ KeyEvent, MouseEvent }
import java.awt.{ Point => APoint }

import _root_.vtk.rendering.{ vtkEventInterceptor, vtkInteractorForwarder }
import _root_.vtk.{ vtkCellPicker, vtkCoordinate, vtkGenericRenderWindowInteractor, vtkProp3D }
import scalismo.geometry.{ Point, _3D }
import scalismo.ui._

class VtkRenderWindowInteractor(parent: VtkPanel, eventForwarder: vtkInteractorForwarder) extends vtkGenericRenderWindowInteractor with EdtPublisher {

  private var height = 0
  private var currentPoint = new APoint
  private var lastPoint = new APoint

  private var shiftPressed = false
  private var controlPressed = false

  // we need the interceptor because the default vtk implementation does not hand on key release events.
  // All methods here return false (thus all events are fully handled, none are actually intercepted).
  class Interceptor extends vtkEventInterceptor {

    override def mouseEntered(e: MouseEvent): Boolean = {
      // grab focus
      if (!parent.canvas.uiComponent.hasFocus) {
        parent.canvas.uiComponent.requestFocusInWindow()
      }
      false
    }

    override def mouseMoved(e: MouseEvent): Boolean = false

    override def mouseDragged(e: MouseEvent): Boolean = false

    override def mousePressed(e: MouseEvent): Boolean = false

    override def mouseReleased(e: MouseEvent): Boolean = false

    override def mouseClicked(e: MouseEvent): Boolean = false

    override def mouseExited(e: MouseEvent): Boolean = false

    override def keyTyped(e: KeyEvent): Boolean = false

    override def keyPressed(e: KeyEvent): Boolean = {
      shiftPressed = (e.getModifiers & 1) == 1
      controlPressed = (e.getModifiers & 2) == 2
      handleModifierButtons()
      false
    }

    override def keyReleased(e: KeyEvent): Boolean = {
      shiftPressed = (e.getModifiers & 1) == 1
      controlPressed = (e.getModifiers & 2) == 2
      handleModifierButtons()
      false
    }

  }

  lazy val interceptor = new Interceptor

  override def ConfigureEvent(): Unit = {
    if (eventForwarder.getEventInterceptor == null) {
      eventForwarder.setEventInterceptor(interceptor)
    }
  }

  val cellPicker = new vtkCellPicker {
    PickFromListOff()
  }
  SetPicker(cellPicker)

  override def SetEventInformationFlipY(x: Int, y: Int, ctrl: Int, shift: Int, unk1: Char, unk2: Int, unk3: String) = {
    currentPoint = new APoint(x, y)
    super.SetEventInformationFlipY(x, y, ctrl, shift, unk1, unk2, unk3)
  }

  protected[vtk] def workspaceOption: Option[Workspace] = parent.workspaceOption

  protected[vtk] def viewportOption: Option[Viewport] = parent.viewportOption

  def renderer = GetRenderWindow().GetRenderers().GetFirstRenderer()

  override def LeftButtonPressEvent() = {
    (workspaceOption, viewportOption) match {
      case (Some(workspace), Some(viewport)) =>
        if (workspace.landmarkClickMode) {
          lastPoint = currentPoint
        }
        //FIXME: this is ugly
        val ok = viewport.onLeftButtonDown(currentPoint)
        if (ok || workspace.landmarkClickMode) {
          super.LeftButtonPressEvent()
          if (!ok) {
            super.LeftButtonReleaseEvent()
          }
        }
      case _ =>
    }
  }

  override def SetSize(width: Int, height: Int) = {
    this.height = height
    super.SetSize(width, height)
  }

  override def LeftButtonReleaseEvent() = {
    (workspaceOption, viewportOption) match {
      case (Some(workspace), Some(viewport)) =>
        if (viewport.onLeftButtonUp(currentPoint)) {
          super.LeftButtonReleaseEvent()
        }

        if (workspace.landmarkClickMode) {
          val threshold = 3 //(pixels)
          if (Math.abs(currentPoint.x - lastPoint.x) < threshold && Math.abs(currentPoint.y - lastPoint.y) < threshold) {
            findPointAndPropAtMousePosition() match {
              case PointAndProp(Some(point), Some(prop)) => prop match {
                case clickable: ClickableActor => clickable.clicked(point)
                case _ =>
              }
              case _ =>
            }
          }
        }
      case _ =>
    }
  }

  override def MiddleButtonPressEvent() = if (viewportOption.forall(_.onMiddleButtonDown(currentPoint))) super.MiddleButtonPressEvent()

  override def MiddleButtonReleaseEvent() = if (viewportOption.forall(_.onMiddleButtonUp(currentPoint))) super.MiddleButtonReleaseEvent()

  override def RightButtonPressEvent() = if (viewportOption.forall(_.onRightButtonDown(currentPoint))) super.RightButtonPressEvent()

  override def RightButtonReleaseEvent() = if (viewportOption.forall(_.onRightButtonUp(currentPoint))) super.RightButtonReleaseEvent()

  override def MouseMoveEvent(): Unit = {
    (workspaceOption, viewportOption) match {
      case (Some(workspace), Some(viewport)) =>
        handleModifierButtons()
        if (viewport.onMouseMove(currentPoint)) {
          super.MouseMoveEvent()
        }
      case _ =>
    }
  }

  private case class PointAndProp(point: Option[Point[_3D]], prop: Option[vtkProp3D])

  private def findPointAndPropAtMousePosition(): PointAndProp = {
    if (cellPicker.Pick(currentPoint.x, height - currentPoint.y - 1, 0.0, renderer) == 1) {
      // simple case
      val prop = cellPicker.GetProp3D()
      val array = cellPicker.GetPickPosition()
      val point = if (array == null) None else Some(Point[_3D](array.map(_.toFloat)))
      PointAndProp(point, Option(prop))
    } else {
      // complicated case
      val coord = new vtkCoordinate
      coord.SetValue(currentPoint.x, height - currentPoint.y - 1, 0.0)
      coord.SetCoordinateSystemToDisplay()
      val array = coord.GetComputedWorldValue(renderer).map(_.toFloat)
      coord.Delete()

      // these coordinates are necessarily inaccurate, because no object was hit, so
      // it's unclear where in space the point is located (in terms of "depth", i.e., how far away).
      // in 3D, there's nothing we can do about this, but in 2D, we know where the slice is, and
      // modify the point accordingly.
      viewportOption match {
        case Some(vp: TwoDViewport) => vp.axis match {
          case Axis.X => array(0) = vp.scene.slicingPosition.x
          case Axis.Y => array(1) = vp.scene.slicingPosition.y
          case Axis.Z => array(2) = vp.scene.slicingPosition.z
        }
        case _ =>
      }
      // return None if the point found is not within the bounding box of the scene
      val point = viewportOption.flatMap { vp =>
        val point = Point[_3D](array)
        val bb = vp.scene.slicingPosition.boundingBox
        if (bb.contains(point)) Some(point) else None
      }
      PointAndProp(point, None)
    }
  }

  private def handleModifierButtons(): Unit = {
    if (shiftPressed || controlPressed) {
      val pointAndProp = findPointAndPropAtMousePosition()

      if (shiftPressed && pointAndProp.point.isDefined) {
        viewportOption.foreach(_.scene.slicingPosition.point = pointAndProp.point.get)
      }

      if (controlPressed) {
        pointAndProp match {
          case PointAndProp(Some(point), Some(prop)) => prop match {
            case imgActor: ImageActor2D =>
              val image = imgActor.source.source
              val pointId = image.domain.findClosestPoint(point)._2
              val intensity = image(pointId).toString.toFloat

              Status.set(StatusMessage(f"(${point(0)}%2.2f,${point(1)}%2.2f,${point(2)}%2.2f) = $intensity%2.2f", log = false))
            case _ =>
          }
          case _ =>
        }
      }
    }
  }
}
