package scalismo.ui.view

import java.awt
import java.awt.event.{ MouseAdapter, MouseEvent }
import java.awt.{ Color, Font, GraphicsEnvironment, Transparency }
import java.text.SimpleDateFormat
import javax.swing._

import scalismo.ui.model.StatusMessage
import scalismo.ui.util.EdtUtil
import HighDpi.scale
import StatusMessage._
import scalismo.ui.view.helper.CustomListCellRenderer

import scala.swing.{ Action, _ }
import scala.util.Try

object StatusBar {

  private[StatusBar] case class UIOptions(color: Color, icon: Icon)

  private final val IconSize = scale(14)

  private def createIcon(partialName: String): Icon = {
    val icon = UIManager.getIcon(s"OptionPane.${partialName}Icon")
    HighDpi.scaleIcon(icon, IconSize, IconSize)
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

class StatusBar extends BorderPanel {

  def set(text: String): Unit = {
    set(StatusMessage(text))
  }

  def set(message: StatusMessage): Unit = EdtUtil.onEdt {
    updateLabelToShowStatusMessage(message, statusLabel.peer, withTimestamp = false)
    if (message.log) logModel.addElement(message)
  }

  def clear(): Unit = {
    statusLabel.icon = null
    statusLabel.text = " "
  }

  def clearLog(): Unit = {
    logModel.clear()
  }

  def showLog(visible: Boolean): Unit = {
    logPanel.visible = visible
  }

  private val statusLabel = new Label(" ") {

    import BorderFactory._

    horizontalAlignment = Alignment.Leading

    border = createCompoundBorder(createEmptyBorder(0, 3, 3, 3), createCompoundBorder(createEtchedBorder(), createEmptyBorder(2, 2, 2, 2)))

    peer.addMouseListener(new MouseAdapter {
      override def mouseClicked(e: MouseEvent): Unit = toggleShowLogAction.apply()
    })
  }

  private val logModel = new DefaultListModel[StatusMessage] {
    val MaxLength = 10000

    override def addElement(e: StatusMessage): Unit = {
      val toRemove = getSize - MaxLength
      if (toRemove >= 0) {
        removeRange(0, toRemove)
      }
      super.addElement(e)
      jLog.ensureIndexIsVisible(getSize - 1)
    }
  }

  private class StatusMessageCellRenderer extends CustomListCellRenderer {
    override def updateListCellRendererComponent(component: awt.Component, list: scala.Any, value: scala.Any, index: Int, isSelected: Boolean, cellHasFocus: Boolean): awt.Component = {
      (component, value) match {
        case (label: JLabel, message: StatusMessage) => updateLabelToShowStatusMessage(message, label, withTimestamp = true)
        case _ => // can't handle
      }
      component
    }
  }

  private val jLog: JList[StatusMessage] = new JList[StatusMessage](logModel) {
    setCellRenderer(new StatusMessageCellRenderer)
  }

  private val log = Component.wrap(jLog)

  private val logPanel = new BorderPanel {
    layout(new ScrollPane(log)) = BorderPanel.Position.Center
    val toolBar = new ToolBar {
      floatable = false
      rollover = false
      rollover = false
    }
    toolBar.add(clearLogAction)
    layout(toolBar) = BorderPanel.Position.North

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

  private val dateFormat = new SimpleDateFormat("HH:mm:ss.SSS")

  private def updateLabelToShowStatusMessage(message: StatusMessage, label: JLabel, withTimestamp: Boolean): Unit = {
    val text = if (withTimestamp) {
      val prefix = dateFormat.format(message.date)
      s"[$prefix] ${message.text}"
    } else message.text

    label.setText(text)
    val options = StatusBar.uiOptions(message.kind)
    label.setForeground(options.color)
    label.setIcon(options.icon)
    label.setFont(label.getFont.deriveFont(if (message.highPriority) font.getStyle | Font.BOLD else font.getStyle & ~Font.BOLD))
  }
}

