package scalismo.ui.rendering.internal

import java.awt.event.{ KeyEvent, MouseEvent }

import vtk.rendering.vtkEventInterceptor

class EventInterceptor extends vtkEventInterceptor() {
  override def keyPressed(e: KeyEvent): Boolean = false

  override def keyTyped(e: KeyEvent): Boolean = false

  override def mouseMoved(e: MouseEvent): Boolean = false

  override def mouseExited(e: MouseEvent): Boolean = false

  override def mouseClicked(e: MouseEvent): Boolean = false

  override def keyReleased(e: KeyEvent): Boolean = false

  override def mouseDragged(e: MouseEvent): Boolean = false

  override def mouseEntered(e: MouseEvent): Boolean = false

  override def mousePressed(e: MouseEvent): Boolean = false

  override def mouseReleased(e: MouseEvent): Boolean = false
}
