/*
 * Copyright (C) 2016  University of Basel, Graphics and Vision Research Group
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scalismo.ui.rendering.internal

import com.jogamp.opengl.{GLAutoDrawable, GLCapabilities, GLEventListener, GLProfile}
import com.jogamp.opengl.awt.GLJPanel

import java.util.concurrent.locks.ReentrantLock
import scalismo.ui.view.ViewportPanel
import vtk._

import java.awt.event.{MouseWheelEvent, MouseWheelListener}

/**
 * An extension of vtkJoglPanelComponent.
 *
 * It provides functionality for controlling (blocking) events and also provides
 * deferred rendering for greater efficiency
 */
class RenderingComponent(viewport: ViewportPanel) extends vtk.rendering.jogl.vtkJoglPanelComponent {

  private lazy val interceptor = new EventInterceptor(viewport.frame)
  private lazy val interactor = new RenderWindowInteractor()
  private lazy val glPanel = new GLJPanelWithViewport(viewport, new GLCapabilities(GLProfile.getMaximum(true)))

  val rendererState = new RendererStateImplementation(renderer, viewport)

  eventForwarder.setEventInterceptor(interceptor)

  interactor.SetRenderWindow(renderWindow)
  interactor.TimerEventResetsTimerOff()
  interactor.ConfigureEvent()
  interactor.SetInteractorStyle(new vtkInteractorStyleTrackballCamera)

  interactor.AddObserver("CreateTimerEvent", eventForwarder, "StartTimer")
  interactor.AddObserver("DestroyTimerEvent", eventForwarder, "DestroyTimer")

  renderWindow.AddRenderer(renderer)

  // Make sure that when VTK internally requests a Render, it gets triggered properly
  renderWindow.AddObserver("WindowFrameEvent", this, "Render")
  renderWindow.GetInteractor.AddObserver("RenderEvent", this, "Render")
  renderWindow.GetInteractor.SetEnableRender(false)

  // Bind interactor forwarder
  glPanel.addMouseListener(eventForwarder)
  glPanel.addMouseMotionListener(eventForwarder)
  glPanel.addKeyListener(eventForwarder)

  object MouseWheelEventInterceptorAdapter extends MouseWheelListener {
    override def mouseWheelMoved(e: MouseWheelEvent): Unit = interceptor.mouseWheelMoved(e)
  }
  glPanel.addMouseWheelListener(MouseWheelEventInterceptorAdapter)

  glPanel.addGLEventListener(new GLEventListener {

    override def init(drawable: GLAutoDrawable): Unit = {
      // Make sure the JOGL context is current
      val ctx = drawable.getContext
      if (!ctx.isCurrent) {
        ctx.makeCurrent()
      }

      // Init VTK OpenGL RenderWindow
      renderWindow.SetPosition(0, 0)

      setSize(drawable.getSurfaceWidth, drawable.getSurfaceHeight)
    }

    override def reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int): Unit = {
      setSize(width, height)
    }

    override def display(drawable: GLAutoDrawable): Unit = {
      inRenderCall = true
      renderWindow.Render()
      inRenderCall = false
    }

    override def dispose(drawable: GLAutoDrawable): Unit = {
      Delete()
      vtkObjectBase.JAVA_OBJECT_MANAGER.gc(false)
    }
  })

  private def executeLocked(block: => Unit): Unit = {
    lock.lock()
    block
    lock.unlock()
  }

  private def executeInterruptibly(block: => Unit): Unit = {
    try {
      lock.lockInterruptibly()
      block
    } catch {
      case _: InterruptedException => // nothing we can do
    } finally {
      lock.unlock()
    }
  }

  override def getComponent: GLJPanel = glPanel

  override def Render(): Unit = {
    if (!inRenderCall) {
      glPanel.repaint()
    }
  }

  override def getRenderer: vtkRenderer = renderer

  override def getVTKLock: ReentrantLock = lock

  override def getRenderWindowInteractor: RenderWindowInteractor = interactor

  override def resetCameraClippingRange(): Unit = {
    if (renderer != null) {
      executeInterruptibly(renderer.ResetCameraClippingRange())
    }
  }

  override def setInteractorStyle(style: vtkInteractorStyle): Unit = {
    if (interactor != null) {
      executeLocked(interactor.SetInteractorStyle(style))
    }
  }

  override def setSize(w: Int, h: Int): Unit = {
    if (renderWindow != null && interactor != null) {
      executeInterruptibly {
        rendererState.setSize(w, h, glPanel)
        renderWindow.SetSize(w, h)
        interactor.SetSize(w, h)
      }
    }
  }

  private val deferred = new DeferredRendering(Render())

  def render(allowDeferred: Boolean = true): Unit = {
    if (!allowDeferred) {
      Render()
    } else {
      deferred.request()
    }
  }
}
