package scalismo.ui.swing

import java.awt
import java.awt.event.{ MouseAdapter, MouseEvent }
import java.awt.{ Color, Font, GraphicsEnvironment, Transparency }
import java.text.SimpleDateFormat
import javax.swing._

import scalismo.ui.{ StatusImplementation, StatusMessage }
import scalismo.ui.StatusMessage._
import scalismo.ui.swing.util.{ UntypedJList, UntypedListCellRenderer, UntypedListModel }
import scalismo.ui.util.EdtUtil

import scala.swing.{ Action, _ }
import scala.util.Try

private[ui] object StatusPanel {

  private[StatusPanel] case class UIOptions(color: Color, icon: Icon)

  private final val IconSize = 14
  lazy val Transparent: ImageIcon = Try {
    val ge = GraphicsEnvironment.getLocalGraphicsEnvironment
    val gd = ge.getDefaultScreenDevice
    val gc = gd.getDefaultConfiguration
    val image = gc.createCompatibleImage(IconSize, IconSize, Transparency.TRANSLUCENT)
    //        val g = image.createGraphics()
    //        g.dispose()
    new ImageIcon(image)
  }.getOrElse(null)

  def createIcon(partialName: String): Icon = {
    UIManager.getIcon(s"OptionPane.${partialName}Icon") match {
      case icon: ImageIcon => new ImageIcon(icon.getImage.getScaledInstance(IconSize, IconSize, java.awt.Image.SCALE_SMOOTH))
      case icon: Icon => Try {
        val (w, h) = (icon.getIconWidth, icon.getIconHeight)
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment
        val gd = ge.getDefaultScreenDevice
        val gc = gd.getDefaultConfiguration
        val image = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT)
        val g = image.createGraphics()
        icon.paintIcon(null, g, 0, 0)
        g.dispose()
        new ImageIcon(image.getScaledInstance(IconSize, IconSize, java.awt.Image.SCALE_SMOOTH))
      }.getOrElse(Transparent)
      case _ => Transparent
    }
  }

  private val uiOptions = {
    val list: List[(StatusMessage.Kind, UIOptions)] = List(
      Information -> UIOptions(Color.BLACK, createIcon("information")),
      Warning -> UIOptions(Color.ORANGE.darker(), createIcon("warning")),
      Error -> UIOptions(Color.RED, createIcon("error")),
      Question -> UIOptions(Color.BLUE, createIcon("question"))
    )
    list.toMap
  }
}

class StatusPanel extends BorderPanel with StatusImplementation {

  StatusImplementation.instance = Some(this)

  private val statusLabel = new Label(" ") {

    import BorderFactory._

    horizontalAlignment = Alignment.Leading

    border = createCompoundBorder(createEmptyBorder(0, 3, 3, 3), createCompoundBorder(createEtchedBorder(), createEmptyBorder(2, 2, 2, 2)))

    peer.addMouseListener(new MouseAdapter {
      override def mouseClicked(e: MouseEvent): Unit = toggleShowLogAction.apply()
    })
  }

  private val logModel = new UntypedListModel {
    val MaxLength = 10000

    def clear(): Unit = {
      peer.removeAllElements()
    }

    override def addElement(e: scala.Any): Unit = {
      val toRemove = peer.getSize - MaxLength
      if (toRemove >= 0) {
        peer.removeRange(0, toRemove)
      }
      super.addElement(e)
      jLog.peer.ensureIndexIsVisible(peer.getSize - 1)
    }
  }

  private val jLog: UntypedJList = new UntypedJList(logModel) {
    setCellRenderer(new UntypedListCellRenderer {
      override def getListCellRendererComponentUntyped(list: scala.Any, value: scala.Any, index: Int, isSelected: Boolean, cellHasFocus: Boolean): awt.Component = {
        val renderer = super.getListCellRendererComponentUntyped(list, value, index, isSelected, cellHasFocus)
        renderer match {
          case label: JLabel =>
            value match {
              case msg: StatusMessage => showMessageInLabel(msg, label, timestamp = true)

              case _ => // not handling
            }
          case _ => //not handling
        }
        renderer
      }
    })
  }
  private val log = Component.wrap(jLog.peer)

  private val logPanel = new BorderPanel {
    layout(new ScrollPane(log)) = BorderPanel.Position.Center
    val toolbar = new Toolbar {
      floatable = false
      rollover = false
      rollover = false
    }
    toolbar.add(clearLogAction)
    layout(toolbar) = BorderPanel.Position.North

    visible = false
  }

  private val bottom = new BorderPanel {
    layout(statusLabel) = BorderPanel.Position.Center
  }

  private lazy val toggleShowLogAction: Action = new Action("@") {
    override def apply(): Unit = {
      logPanel.visible = !logPanel.visible
      bottom.peer.revalidate()
    }
  }

  private lazy val clearLogAction: Action = new Action("Clear Log") {
    override def apply(): Unit = logModel.clear()
  }

  layout(bottom) = BorderPanel.Position.South
  layout(logPanel) = BorderPanel.Position.Center

  override def set(message: StatusMessage): Unit = EdtUtil.onEdt {
    showMessageInLabel(message, statusLabel.peer, timestamp = false)
    if (message.log) logModel.addElement(message)
  }

  override def clear(): Unit = {
    statusLabel.icon = null
    statusLabel.text = " "
  }

  def clearLog(): Unit = {
    logModel.clear()
  }

  def showLog(visible: Boolean): Unit = {
    logPanel.visible = visible
  }

  private val dateFormat = new SimpleDateFormat("HH:mm:ss.SSS")

  private def showMessageInLabel(message: StatusMessage, label: JLabel, timestamp: Boolean): Unit = {
    val text = if (timestamp) {
      val prefix = dateFormat.format(message.date)
      s"[$prefix] ${message.text}"
    } else message.text

    label.setText(text)
    val options = StatusPanel.uiOptions(message.kind)
    label.setForeground(options.color)
    label.setIcon(options.icon)
    label.setFont(label.getFont.deriveFont(if (message.highPriority) font.getStyle | Font.BOLD else font.getStyle & ~Font.BOLD))
  }
}

