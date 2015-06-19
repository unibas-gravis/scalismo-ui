package scalismo.ui.swing.props

import java.awt.{ Color, Dimension, Graphics }
import javax.swing.JPanel
import javax.swing.border.{ LineBorder, TitledBorder }
import javax.swing.event.{ ChangeEvent, ChangeListener }

import scalismo.ui.swing.util.ColorPickerPanel
import scalismo.ui.visualization.VisualizationProperty
import scalismo.ui.visualization.props.{ HasColor, HasOpacity, OpacityProperty }
import scalismo.ui.{ Constants, EdtPublisher }

import scala.swing.event.Event
import scala.swing.{ BorderPanel, Component }

class ColorPropertyPanel extends BorderPanel with PropertyPanel {
  override def description: String = "Color"

  private var target: Option[HasColor] = None

  case class ColorChosen(color: Color) extends Event

  class ColorDisplayer extends Component {
    val BorderWidth = 1
    override lazy val peer = new JPanel {
      override def paintComponent(g: Graphics) {
        val dim: Dimension = getSize
        val s = BorderWidth
        g.setColor(Constants.Visualization.PerceivedBackgroundColor)
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
    peer.setPreferredSize(new Dimension(20, 20))
    peer.setBorder(new LineBorder(Color.BLACK, BorderWidth, false))
  }

  val colorDisplayer = new ColorDisplayer

  class ColorChooser extends Component with ChangeListener with EdtPublisher {
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
        publishEdt(ColorChosen(c))
      }
    }

    border = new javax.swing.border.EmptyBorder(10, 0, 0, 0)
  }

  val colorChooser = new ColorChooser

  {
    val northedPanel = new BorderPanel {
      val colorPanel = new BorderPanel {
        border = new TitledBorder(null, "Color", TitledBorder.LEADING, 0, null, null)
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
    if (target.isDefined) {
      deafToOwnEvents()
      updateColorDisplayer()
      listenToOwnEvents()
    }
  }

  def updateColorDisplayer() {
    val c = target.get.color.value
    colorChooser.setColor(c)
    colorDisplayer.setColor(c, targetOpacityOption().map {
      _.value
    }.getOrElse(1.0f))
  }

  // returns the target's opacity property if the target also happens to be a HasOpacity, else None
  def targetOpacityOption(): Option[OpacityProperty] = {
    target match {
      case Some(o: HasOpacity) => Some(o.opacity)
      case _ => None
    }
  }

  override def setObject(obj: Option[AnyRef]): Boolean = {
    cleanup()
    obj match {
      case Some(c: HasColor) =>
        target = Some(c)
        listenTo(c.color)
        targetOpacityOption().foreach(o => listenTo(o))
        updateUi()
        true
      case _ => false
    }
  }

  def cleanup(): Unit = {
    target.foreach(t => deafTo(t.color))
    targetOpacityOption().foreach(o => deafTo(o))
    target = None
  }

  reactions += {
    case VisualizationProperty.ValueChanged(_) => updateUi()
    case ColorChosen(c) =>
      if (target.isDefined) {
        target.get.color.value = c
        updateColorDisplayer()
      }
  }

}
