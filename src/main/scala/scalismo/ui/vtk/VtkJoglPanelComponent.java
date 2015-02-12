package scalismo.ui.vtk;

import vtk.rendering.vtkAbstractComponent;
import vtk.rendering.vtkComponent;
import vtk.rendering.vtkInteractorForwarder;
import vtk.*;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLJPanel;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is essentially the same as the original vtk.vtkJoglPanelComponent class, except that
 * the interactor is a VtkRenderWindowInteractor, and that it contains a workaround to
 * prevent deadlocks on Windows.
 */

public class VtkJoglPanelComponent implements vtkComponent<GLJPanel> {
    protected vtkGenericOpenGLRenderWindow renderWindow;
    protected vtkRenderer renderer;
    protected vtkCamera camera;
    protected VtkRenderWindowInteractor interactor;
    protected vtkInteractorForwarder eventForwarder;
    protected ReentrantLock lock;
    protected boolean inRenderCall;

    protected GLJPanel uiComponent;
    protected boolean isWindowCreated;
    protected GLEventListener glEventListener;

    public VtkJoglPanelComponent(VtkPanel panel) {
        final VtkJoglPanelComponent self = this;

        inRenderCall = false;
        renderWindow = new vtkGenericOpenGLRenderWindow();
        renderer = new vtkRenderer();
        interactor = new VtkRenderWindowInteractor(panel);
        lock = new ReentrantLock();

        // Init interactor
        interactor.SetRenderWindow(renderWindow);
        interactor.TimerEventResetsTimerOff();
        interactor.SetSize(200, 200);
        interactor.ConfigureEvent();

        // Update style
        vtkInteractorStyleTrackballCamera style = new vtkInteractorStyleTrackballCamera();
        interactor.SetInteractorStyle(style);

        // Setup event forwarder
        eventForwarder = new vtkInteractorForwarder(this);
        interactor.AddObserver("CreateTimerEvent", eventForwarder, "StartTimer");
        interactor.AddObserver("DestroyTimerEvent", eventForwarder, "DestroyTimer");

        // Link renderWindow with renderer
        renderWindow.AddRenderer(renderer);

        // Keep camera around to prevent its creation/deletion in Java world
        camera = renderer.GetActiveCamera();


        isWindowCreated = false;
        uiComponent = new GLJPanel(new GLCapabilities(GLProfile.getDefault()));
        renderWindow.SetIsDirect(1);
        renderWindow.SetSupportsOpenGL(1);
        renderWindow.SetIsCurrent(true);

        // Create the JOGL Event Listener
        glEventListener = new GLEventListener() {
            public void init(GLAutoDrawable drawable) {
                self.isWindowCreated = true;

                // Make sure the JOGL Context is current
                GLContext ctx = drawable.getContext();
                if (!ctx.isCurrent()) {
                    ctx.makeCurrent();
                }

                self.renderWindow.SetPosition(0, 0);
                self.setSize(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
                self.renderWindow.OpenGLInit();
            }

            public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
                self.setSize(width, height);
            }

            public void display(GLAutoDrawable drawable) {
                self.inRenderCall = true;
                self.renderWindow.Render();
                self.inRenderCall = false;
            }

            public void dispose(GLAutoDrawable drawable) {
                self.Delete();
                vtkObject.JAVA_OBJECT_MANAGER.gc(false);
            }
        };

        // Bind the interactor forwarder
        vtkInteractorForwarder forwarder = getInteractorForwarder();
        uiComponent.addMouseListener(forwarder);
        uiComponent.addMouseMotionListener(forwarder);
        uiComponent.addKeyListener(forwarder);

        // Make sure when VTK internaly request a Render, the Render get
        // properly triggered
        renderWindow.AddObserver("WindowFrameEvent", this, "Render");
        renderWindow.GetInteractor().AddObserver("RenderEvent", this, "Render");
        renderWindow.GetInteractor().SetEnableRender(false);

        getComponent().addGLEventListener(glEventListener);
    }

    public ReentrantLock getVTKLock() {
        return lock;
    }

    public void resetCamera() {
        if (renderer == null) {
            return; // Nothing to do we are deleted...
        }

        try {
            lock.lockInterruptibly();
            renderer.ResetCamera();
        } catch (InterruptedException e) {
            // Nothing that we can do
        } finally {
            lock.unlock();
        }
    }

    public void resetCameraClippingRange() {
        if (renderWindow == null) {
            return; // Nothing to do we are deleted...
        }

        try {
            lock.lockInterruptibly();
            renderer.ResetCameraClippingRange();
        } catch (InterruptedException e) {
            // Nothing that we can do
        } finally {
            lock.unlock();
        }
    }

    public vtkCamera getActiveCamera() {
        return camera;
    }

    public vtkRenderer getRenderer() {
        return renderer;
    }

    public vtkRenderWindow getRenderWindow() {
        return renderWindow;
    }

    public vtkGenericRenderWindowInteractor getRenderWindowInteractor() {
        return interactor;
    }

    public void setInteractorStyle(vtkInteractorStyle style) {
        if (interactor != null) {
            lock.lock();
            interactor.SetInteractorStyle(style);
            lock.unlock();
        }
    }

    public void setSize(int w, int h) {
        if (renderWindow == null || interactor == null) {
            return; // Nothing to do we are deleted...
        }

        try {
            lock.lockInterruptibly();
            renderWindow.SetSize(w, h);
            interactor.SetSize(w, h);
        } catch (InterruptedException e) {
            // Nothing that we can do
        } finally {
            lock.unlock();
        }
    }

    public void Delete() {
        lock.lock();
        renderer = null;
        camera = null;
        interactor = null;
        // removing the renderWindow is let to the superclass
        // because in the very special case of an AWT component
        // under Linux, destroying renderWindow crashes.
        lock.unlock();
    }

    public vtkInteractorForwarder getInteractorForwarder() {
        return eventForwarder;
    }

    public void attachOrientationAxes() {
        vtkAbstractComponent.attachOrientationAxes(this);
    }

    public GLJPanel getComponent() {
        return uiComponent;
    }

    /**
     * Render the internal component
     */
    public void Render() {
        // Make sure we can render
        if (!inRenderCall) {
            uiComponent.repaint();
        }
    }

    /**
     * @return true if the graphical component has been properly set and
     * operation can be performed on it.
     */
    public boolean isWindowSet() {
        return isWindowCreated;
    }
}
