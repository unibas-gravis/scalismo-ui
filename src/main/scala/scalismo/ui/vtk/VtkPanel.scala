package scalismo.ui.vtk

import java.awt.BorderLayout
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JPanel

import scalismo.ui.swing.ViewportPanel
import scalismo.ui.{ EdtPublisher, Viewport, Workspace }

import scala.swing.Component
import scala.util.Try

class VtkPanel extends Component with EdtPublisher {
  lazy val canvas = new VtkCanvas(this)
  lazy val vtkViewport: VtkViewport = new VtkViewport(this, canvas.getRenderer)
  listenTo(vtkViewport)

  protected[vtk] var viewportOption: Option[Viewport] = None
  protected[vtk] var workspaceOption: Option[Workspace] = None

  override lazy val peer = {
    val panel = new JPanel(new BorderLayout())
    panel.add(canvas.getComponent, BorderLayout.CENTER)
    panel
  }

  reactions += {
    case VtkContext.RenderRequest(s, immediately) =>
      canvas.render(immediately)
    case VtkContext.ResetCameraRequest(s) =>
      resetCamera()
  }

  def attach(source: ViewportPanel) = {
    viewportOption = source.viewportOption
    workspaceOption = source.workspaceOption
    vtkViewport.attach()
    workspaceOption.map(listenTo(_))
  }

  def detach() = {
    workspaceOption.map(deafTo(_))
    vtkViewport.detach()
    canvas.disableDeferredRendering()
    workspaceOption = None
    viewportOption = None
  }

  def resetCamera() = {
    vtkViewport.resetCamera()
  }

  def screenshot(file: File): Try[Unit] = Try {
    val source = canvas.uiComponent
    val image = new BufferedImage(source.getWidth, source.getHeight, BufferedImage.TYPE_INT_RGB)
    val g = image.createGraphics()

    // parameter description: see
    // https://jogamp.org/deployment/jogamp-next/javadoc/jogl/javadoc/javax/media/opengl/awt/GLJPanel.html#setupPrint%28double,%20double,%20int,%20int,%20int%29
    source.setupPrint(1, 1, 0, -1, -1)
    source.printAll(g)
    source.releasePrint()

    image.flush()
    ImageIO.write(image, "png", file)
  }
}
