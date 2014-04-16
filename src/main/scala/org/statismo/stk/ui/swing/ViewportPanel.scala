package org.statismo.stk.ui.swing

import java.io.File
import scala.swing._
import scala.swing.BorderPanel.Position.Center
import scala.util.Try
import org.statismo.stk.ui._
import org.statismo.stk.ui.swing.actions.SaveAction
import javax.swing.border.TitledBorder
import javax.swing._
import scala.swing.event.ValueChanged
import java.awt.{Color, Graphics}
import scala.collection.immutable
import scala.util.Failure
import scala.Some
import scala.swing.Action
import java.awt.event.{ComponentEvent, ComponentListener}

object ViewportRenderingPanelPool {
  val jpanel = new JPanel {
    setLayout(null)

    override def paintComponent(g: Graphics) = {}

    override def paintBorder(g: Graphics) = {}
  }

  private[ViewportRenderingPanelPool] class Entry(val panel: ViewportRenderingPanel, var available: Boolean = true)

  private var pool: immutable.Seq[Entry] = Nil

  def allocate: ViewportRenderingPanel = this.synchronized {
    val entry = pool.find(e => e.available).getOrElse {
      val panel: ViewportRenderingPanel = new DummyViewportRenderingPanel
      val entry = new Entry(panel)
      pool ++= immutable.Seq(entry)
      jpanel.add(panel.target)
      entry
    }
    entry.available = false
    entry.panel
  }

  def free(panel: ViewportRenderingPanel): Unit = this.synchronized {
    pool.find(e => e.panel eq panel).map {
      entry =>
        entry.available = true
    }
  }
}

trait ViewportRenderingPanel extends ComponentListener {
  def screenshot(file: File): Try[Unit]

  def resetCamera(): Unit

  def target: java.awt.Component

  private var source: Option[ViewportPanel] = None

  def attach(source: ViewportPanel): Unit = this.synchronized {
    detach()
    this.source = Some(source)
    target.setVisible(true)
    source.renderStub.peer.addComponentListener(this)
    sourceVisibilityChanged
    sourcePositionChanged
  }

  def detach(): Unit = this.synchronized {
    source.map {
      s =>
        s.renderStub.peer.removeComponentListener(this)
        source = None
        target.setVisible(false)
        ViewportRenderingPanelPool.free(this)
    }
    source = None
  }

  override def componentHidden(e: ComponentEvent) = sourceVisibilityChanged

  override def componentShown(e: ComponentEvent) = sourceVisibilityChanged

  override def componentMoved(e: ComponentEvent) = sourcePositionChanged

  override def componentResized(e: ComponentEvent) = sourcePositionChanged

  protected def sourceVisibilityChanged = {
    source.map {
      s =>
        val p = s.renderStub.peer
        target.setVisible(p.isVisible)
    }
  }

  protected def sourcePositionChanged = {
    source.map {
      s =>
        val p = s.renderStub.peer
        target.setBounds(SwingUtilities.convertRectangle(p.getParent, p.getBounds, target.getParent))
        target.getParent.getParent.invalidate()
        target.getParent.getParent.repaint()
    }
  }
}

class DummyViewportRenderingPanel extends ViewportRenderingPanel {
  override lazy val target = new java.awt.Component {
    override def paint(g: Graphics) = {
      g.setColor(Color.RED)
      g.fillRect(0, 0, getWidth, getHeight)
    }
  }

  override def resetCamera() = {}

  override def screenshot(file: File) = Try(())
}

class ViewportPanel extends BorderPanel {

  protected var viewport: Option[Viewport] = None
  protected var workspace: Option[Workspace] = None
  protected var renderer: Option[ViewportRenderingPanel] = None

  def viewportOption: Option[Viewport] = viewport
  def workspaceOption: Option[Workspace] = workspace

  def show(workspace: Workspace, viewport: Viewport) = this.synchronized {
    hide() // just in case someone forgot to call it
    this.viewport = Some(viewport)
    this.workspace = Some(workspace)
    listenTo(viewport)
    title.setTitle(viewport.name)
    renderer = Some(ViewportRenderingPanelPool.allocate)
    renderer.get.attach(this)
  }

  def hide() = this.synchronized {
    viewport.map {
      v =>
        deafTo(v)
    }
    renderer.map(_.detach())
    renderer = None
    viewport = None
    workspace = None
  }

  val title = new TitledBorder(null, "", TitledBorder.LEADING, 0, null, null)
  border = title

  protected[ui] val renderStub = new Component {
    override lazy val peer = new JComponent {
      override def paint(g: Graphics) = {
        g.setColor(Color.BLACK)
        g.fillRect(0, 0, getWidth, getHeight)
      }
    }
  }

  layout(renderStub) = Center

  reactions += {
    case Nameable.NameChanged(v) =>
      viewport match {
        case None =>
        case Some(vp) =>
          if (v eq vp) {
            title.setTitle(vp.name)
            revalidate()
          }
      }
  }

  val toolbar = new Toolbar {
    floatable = false
    rollover = true
    orientation = Orientation.Horizontal
  }

  toolbar.add(new Action("SS") {
    def doSave(file: File): Try[Unit] = renderer.map(_.screenshot(file)).getOrElse(Failure(new IllegalStateException("no renderer")))

    override def apply() = {
      new SaveAction(doSave, PngFileIoMetadata).apply()
    }
  })

}

class ThreeDViewportPanel extends ViewportPanel {
  toolbar.add(new Action("RC") {
    override def apply() = {
      renderer.map(_.resetCamera())
    }
  })
  layout(toolbar) = BorderPanel.Position.North
}

class TwoDViewportPanel extends ViewportPanel {
  toolbar.add(new Action("RC") {
    override def apply() = {
      renderer.map(_.resetCamera())
    }
  })

  override def show(workspace: Workspace, viewport: Viewport) = {
    super.show(workspace, viewport)
    slider.update(viewport.scene.slicingPosition)
    slider.listenTo(viewport.scene)
  }

  override def hide() = {
    viewport map {
      vp => slider.deafTo(vp.scene)
    }
    super.hide()
  }

  private[TwoDViewportPanel] class VpSlider extends Slider {

    import org.statismo.stk.ui.Scene.SlicingPosition.Precision.valueToPrecisionVal

    peer.setOrientation(SwingConstants.VERTICAL)

    reactions += {
      case Scene.SlicingPosition.PointChanged(sp) => update(sp)
      case Scene.SlicingPosition.PrecisionChanged(sp) => update(sp)
      case Scene.SlicingPosition.BoundingBoxChanged(sp) => update(sp)
      case ValueChanged(c) if c eq this =>
        viewport match {
          case Some(vp: TwoDViewport) =>
            val sp = vp.scene.slicingPosition
            val value = sp.precision.fromInt(this.value)
            vp.axis match {
              case Axis.X => sp.x = value
              case Axis.Y => sp.y = value
              case Axis.Z => sp.z = value
            }
          case _ =>
        }
    }

    def update(sp: Scene.SlicingPosition) = {
      deafTo(this)
      viewport match {
        case Some(vp: TwoDViewport) =>
          val (min, max, value) = vp.axis match {
            case Axis.X => (sp.boundingBox.xMin, sp.boundingBox.xMax, sp.x)
            case Axis.Y => (sp.boundingBox.yMin, sp.boundingBox.yMax, sp.y)
            case Axis.Z => (sp.boundingBox.zMin, sp.boundingBox.zMax, sp.z)
          }
          this.min = sp.precision.toIntValue(min)
          this.max = sp.precision.toIntValue(max)
          this.value = sp.precision.toIntValue(value)
        case _ =>
      }
      listenTo(this)
    }
  }

  private val slider = new VpSlider
  layout(slider) = BorderPanel.Position.East
  layout(toolbar) = BorderPanel.Position.North
}
