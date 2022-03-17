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
import vtk.rendering.jogl.vtkJoglPanelComponent

/**
 * This is essentially a Scala re-implementation based on `vtk.rendering.jogl.vtkJoglPanelComponent`.
 * It includes a couple of bugfixes and extensions.
 */
class RenderingComponent(viewport: ViewportPanel) extends vtk.rendering.vtkComponent[GLJPanel] {

  ////// fields / constructor

  private val renderWindow = new vtkJoglPanelComponent().getRenderWindow()//new vtkGenericOpenGLRenderWindow()
  private val lock = new ReentrantLock
  private var renderer = new vtkRenderer

  // Keep camera around to prevent its creation/deletion in Java world
  private var camera = renderer.GetActiveCamera()

  private val interceptor = new EventInterceptor(viewport.frame)
  private val eventForwarder = new InteractorForwarder(this)
  eventForwarder.setEventInterceptor(interceptor)

  val rendererState = new RendererStateImplementation(renderer, viewport)

  private var interactor = new RenderWindowInteractor()
  private val glPanel = new GLJPanelWithViewport(viewport, new GLCapabilities(GLProfile.getDefault()))

  private var inRenderCall = false

  // Initialize interactor
  interactor.SetRenderWindow(renderWindow)
  interactor.TimerEventResetsTimerOff()
  interactor.ConfigureEvent()
  interactor.SetInteractorStyle(new vtkInteractorStyleTrackballCamera)

  // Setup event forwarder
  interactor.AddObserver("CreateTimerEvent", eventForwarder, "StartTimer")
  interactor.AddObserver("DestroyTimerEvent", eventForwarder, "DestroyTimer")

  // Link renderWindow with renderer
  renderWindow.AddRenderer(renderer)

//  renderWindow.SetIsDirect(1)
//  renderWindow.SetSupportsOpenGL(1)
//  renderWindow.SetIsCurrent(true)

  // Make sure that when VTK internally requests a Render, it gets triggered properly
  renderWindow.AddObserver("WindowFrameEvent", this, "Render")
  renderWindow.GetInteractor.AddObserver("RenderEvent", this, "Render")
  renderWindow.GetInteractor.SetEnableRender(false)

  // Bind interactor forwarder
  glPanel.addMouseListener(eventForwarder)
  glPanel.addMouseMotionListener(eventForwarder)
  glPanel.addKeyListener(eventForwarder)
  glPanel.addMouseWheelListener(eventForwarder)

  glPanel.addGLEventListener(new GLEventListener {

    override def init(drawable: GLAutoDrawable): Unit = {
      // Make sure the JOGL context is current
      val ctx = drawable.getContext
      if (!ctx.isCurrent) {
        ctx.makeCurrent()
      }

      // Init VTK OpenGL RenderWindow

      // renderWindow.SetMapped(1) // this is there in the original code, but causes DEADLOCKs on Windows.
      renderWindow.SetPosition(0, 0)
      //renderWindow.OpenGLInit()

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

  ////// helper methods

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

  ////// overrides

  override def Delete(): Unit = executeLocked {
    renderer = null
    camera = null
    interactor = null
    /* ORIGINAL COMMENT FROM vtk.rendering.vtkAbstractComponent.java:
   * removing the renderWindow is let to the superclass
   * because in the very special case of an AWT component
   * under Linux, destroying renderWindow crashes.
   */
  }

  override def getActiveCamera: vtkCamera = camera

  override def getRenderWindow: vtkRenderWindow = renderWindow

  override def getInteractorForwarder: InteractorForwarder = eventForwarder

  override def resetCamera(): Unit = {
    if (renderer != null) {
      executeInterruptibly {
        renderer.ResetCamera()
      }
    }
    Render()
  }

  override def getRenderer: vtkRenderer = renderer

  override def getVTKLock: ReentrantLock = lock

  override def getRenderWindowInteractor: RenderWindowInteractor = interactor

  override def getComponent: GLJPanel = glPanel

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

  override def Render(): Unit = {
    if (!inRenderCall) {
      glPanel.repaint()
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
