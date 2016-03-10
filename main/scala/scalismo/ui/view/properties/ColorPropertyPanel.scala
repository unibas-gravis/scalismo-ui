package scalismo.ui.view.properties

import java.awt.{ Color, Dimension, Graphics }
import javax.swing.JPanel
import javax.swing.border.{ LineBorder, TitledBorder }
import javax.swing.event.{ ChangeEvent, ChangeListener }

import scalismo.ui.event.ScalismoPublisher
import scalismo.ui.model.SceneNode
import scalismo.ui.model.properties.{ HasColor, HasOpacity, NodeProperty, OpacityProperty }
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.swing.ColorPickerPanel
import scalismo.ui.view.util.{ Constants, ScalableUI }

import scala.swing.event.Event
import scala.swing.{ BorderPanel, Component }

object ColorPropertyPanel extends PropertyPanel.Factory {
  override def create(frame: ScalismoFrame): PropertyPanel = {
    new ColorPropertyPanel(frame)
  }
}

class ColorPropertyPanel(override val frame: ScalismoFrame) extends BorderPanel with PropertyPanel {
  override def description: String = "Color"

  private var targets: List[HasColor] = Nil

  case class ColorChosen(color: Color) extends Event

  class ColorDisplayer extends Component {
    val BorderWidth = ScalableUI.scale(1)
    override lazy val peer = new JPanel {
      override def paintComponent(g: Graphics): Unit = {
        val dim: Dimension = getSize
        val s = BorderWidth
        g.setColor(Constants.PerceivedBackgroundColor)
        g.fillRect(s, s, dim.width - s, dim.height - s)
        // now paint the selected color on the gray background
        g.setColor(getBackground)
        g.fillRect(s, s, dim.width - s, dim.height - s)
      }
    }

    def setColor(color: Color, opacity: Float) = {
      val comp = color.getColorComponents(null)
      val c = new Color(comp(0), comp(1), comp(2), opacity)
      peer.setBackground(c)
      peer.setForeground(c)
      revalidate()
      repaint()
    }

    peer.setOpaque(false)
    peer.setPreferredSize(ScalableUI.scaleDimension(new Dimension(20, 20)))
    peer.setBorder(new LineBorder(Color.BLACK, BorderWidth, false))
  }

  val colorDisplayer = new ColorDisplayer

  class ColorChooser extends Component with ChangeListener with ScalismoPublisher {
    override lazy val peer = new ColorPickerPanel()
    private var deaf = false
    setColor(Color.WHITE)
    peer.addChangeListener(this)

    def setColor(c: Color) = {
      deaf = true
      peer.setRGB(c.getRed, c.getGreen, c.getBlue)
      deaf = false
    }

    def stateChanged(event: ChangeEvent) = {
      if (!deaf) {
        val rgb = peer.getRGB
        val c: Color = new Color(rgb(0), rgb(1), rgb(2))
        publishEvent(ColorChosen(c))
      }
    }

    border = new javax.swing.border.EmptyBorder(10, 0, 0, 0)
  }

  val colorChooser = new ColorChooser

  {
    val northedPanel = new BorderPanel {
      val colorPanel = new BorderPanel {
        border = new TitledBorder(null, description, TitledBorder.LEADING, 0, null, null)
        layout(colorChooser) = BorderPanel.Position.Center
        layout(colorDisplayer) = BorderPanel.Position.North
      }
      layout(colorPanel) = BorderPanel.Position.Center
    }
    layout(northedPanel) = BorderPanel.Position.North
  }

  listenToOwnEvents()

  def listenToOwnEvents() = {
    listenTo(colorChooser)
  }

  def deafToOwnEvents() = {
    deafTo(colorChooser)
  }

  def updateUi() = {
    if (targets.nonEmpty) {
      deafToOwnEvents()
      updateColorDisplayer()
      listenToOwnEvents()
    }
  }

  def updateColorDisplayer(): Unit = {
    targets.headOption.foreach { t =>
      val c = t.color.value
      colorChooser.setColor(c)
      colorDisplayer.setColor(c, targetOpacityOption().map(_.value).getOrElse(1.0f))
    }
  }

  // returns the target's opacity property if the (first) target also happens to be a HasOpacity, else None
  def targetOpacityOption(): Option[OpacityProperty] = {
    targets.headOption.collect { case ok: HasOpacity => ok.opacity }
  }

  override def setNodes(nodes: List[SceneNode]): Boolean = {
    cleanup()
    val supported = allOf[HasColor](nodes)
    if (supported.nonEmpty) {
      targets = supported
      listenTo(targets.head.color)
      targetOpacityOption().foreach(o => listenTo(o))
      updateUi()
      true
    } else false
  }

  def cleanup(): Unit = {
    targets.headOption.foreach(t => deafTo(t.color))
    targetOpacityOption().foreach(o => deafTo(o))
    targets = Nil
  }

  reactions += {
    case NodeProperty.event.PropertyChanged(_) => updateUi()
    case ColorChosen(c) =>
      targets.foreach { t =>
        t.color.value = c
        updateColorDisplayer()
      }
  }

}