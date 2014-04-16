package org.statismo.stk.ui.swing

import scala.swing._

import org.statismo.stk.ui._
import javax.swing.{JLabel, JButton, OverlayLayout, JPanel}
import scala.collection.{immutable, mutable}
import org.statismo.stk.ui.swing.CardPanel.CardableComponent
import java.awt.{Color, Graphics}

class PerspectivesPanel(val workspace: Workspace) extends BorderPanel {
  listenTo(workspace.scene)

  reactions += {
    case Scene.PerspectiveChanged(s) =>
      updateUi()
      revalidate()
  }

  private[PerspectivesPanel] class TrackingCardPanel extends CardPanel {
    PerspectivePanels.mappings.foreach {
      case (f, i) =>
        this.add(i)
    }

    private var currentPanel: Option[PerspectivePanel] = None

    def set(c: PerspectivePanel) = {
      currentPanel = Some(c)
      super.show(c)
    }

    def get(): Option[PerspectivePanel] = currentPanel
  }

  private val cards = new TrackingCardPanel
  private val center = new Panel {
    override lazy val peer = new JPanel() {
      override def paintComponent(g: Graphics) = {
        g.setColor(Color.BLUE)
        g.fillRect(0, 0, getWidth, getHeight)
      }
    }
    peer.setLayout(new OverlayLayout(peer))
    peer.add(ViewportRenderingPanelPool.jpanel)
    peer.add(cards.peer)
  }
  layout(center) = BorderPanel.Position.Center
  updateUi()

  def updateUi() = {
    val next = PerspectivePanels.mappings.get(workspace.scene.perspective.factory)
    next match {
      case None =>
        System.err.println("PerspectivesPanel: Don't know how to display perspective of class " + workspace.scene.perspective.getClass.getName)
      case Some(panel) =>
        cards.get().map {
          _.hide()
        }
        cards.set(panel)
        panel.show(workspace)
    }
    workspace.scene.perspectiveChangeCompleted()
  }
}

object PerspectivePanels {
  val mappings: mutable.HashMap[PerspectiveFactory, PerspectivePanel] = new mutable.HashMap()
  mappings.put(SingleViewportPerspective, new SingleThreeDViewportPanel)
  mappings.put(XOnlyPerspective, new SingleTwoDViewportPanel)
  mappings.put(YOnlyPerspective, new SingleTwoDViewportPanel)
  mappings.put(ZOnlyPerspective, new SingleTwoDViewportPanel)
  mappings.put(SlicerAltPerspective, new SlicerAltViewportsPanel)
  mappings.put(FourViewportsPerspective, new FourViewportsPanel)
  mappings.put(TwoViewportsPerspective, new TwoViewportsPanel)
}

trait PerspectivePanel extends CardPanel.CardableComponent {
  def show(workspace: Workspace): Unit = {
    val zipped = viewportPanels.zip(workspace.scene.viewports)
    zipped.foreach {
      case (panel, viewport) =>
        panel.show(workspace, viewport)
    }
    workspace.publish(Workspace.RenderOnScreen(workspace))
  }

  def hide(): Unit = {
    viewportPanels.foreach(_.hide())
  }

  val viewportPanels: immutable.Seq[ViewportPanel]
}

class SingleTwoDViewportPanel extends BorderPanel with PerspectivePanel {
  override lazy val viewportPanels = immutable.Seq(new TwoDViewportPanel())
  layout(viewportPanels.head) = BorderPanel.Position.Center
}

class SingleThreeDViewportPanel extends BorderPanel with PerspectivePanel {
  override lazy val viewportPanels = immutable.Seq(new ThreeDViewportPanel())
  layout(viewportPanels.head) = BorderPanel.Position.Center
}

class SlicerAltViewportsPanel extends GridPanel(2, 2) with PerspectivePanel {
  override lazy val viewportPanels = immutable.Seq(new ThreeDViewportPanel, new TwoDViewportPanel, new TwoDViewportPanel, new TwoDViewportPanel)
  viewportPanels.foreach {
    p => this.contents += p
  }
}

class FourViewportsPanel extends GridPanel(2, 2) with PerspectivePanel {
  override lazy val viewportPanels = immutable.Seq(new ThreeDViewportPanel, new ThreeDViewportPanel, new ThreeDViewportPanel, new ThreeDViewportPanel)
  viewportPanels.foreach {
    p => this.contents += p
  }
}

class TwoViewportsPanel extends GridPanel(1, 2) with PerspectivePanel {
  override lazy val viewportPanels = immutable.Seq(new ThreeDViewportPanel, new ThreeDViewportPanel)
  viewportPanels.foreach {
    p => this.contents += p
  }
}
