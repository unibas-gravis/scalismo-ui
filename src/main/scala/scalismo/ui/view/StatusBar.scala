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

package scalismo.ui.view

import java.awt.event.{ MouseAdapter, MouseEvent }
import java.awt.{ Color, Cursor, Font, Component => AComponent }
import java.text.SimpleDateFormat
import javax.swing._

import scalismo.ui.model.StatusMessage
import scalismo.ui.model.StatusMessage._
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.util.EdtUtil
import scalismo.ui.view.swing.CustomListCellRenderer
import scalismo.ui.view.util.ScalableUI

import scala.swing.event.Key
import scala.swing.{ Action, _ }

object StatusBar {

  private[StatusBar] case class UIOptions(textColor: Color, icon: Icon)

  private val uiOptions = {
    val list: List[(StatusMessage.Kind, UIOptions)] = List(
      Information -> UIOptions(Color.BLACK, BundledIcon.Information.standardSized()),
      Warning -> UIOptions(Color.BLACK, BundledIcon.Warning.standardSized()),
      Error -> UIOptions(Color.BLACK, BundledIcon.Error.standardSized()),
      Question -> UIOptions(Color.BLACK, BundledIcon.Question.standardSized())
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

  private val statusLabel = new Label("Welcome to the Scalismo Viewer!", BundledIcon.Smiley.standardSized(), Alignment.Leading) {

    import BorderFactory._

    val _3 = ScalableUI.scale(3)
    val _2 = ScalableUI.scale(2)
    border = createCompoundBorder(createEmptyBorder(0, _3, _3, _3), createCompoundBorder(createEtchedBorder(), createEmptyBorder(_2, _2, _2, _2)))

    cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

    tooltip = "Click on the status bar to show the history of messages"

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
    override def updateListCellRendererComponent(component: AComponent, list: scala.Any, value: scala.Any, index: Int, isSelected: Boolean, cellHasFocus: Boolean): AComponent = {
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
    toolBar.add(new Button(clearLogAction) {
      icon = BundledIcon.Remove.standardSized()
    })
    layout(toolBar) = BorderPanel.Position.North

    visible = false
  }

  private val bottom = new BorderPanel {
    layout(statusLabel) = BorderPanel.Position.Center
  }

  private lazy val toggleShowLogAction: Action = new Action("") {
    override def apply(): Unit = {
      logPanel.visible = !logPanel.visible
      bottom.peer.revalidate()
    }
  }

  private lazy val clearLogAction: Action = new Action("Clear History") {
    mnemonic = Key.C.id

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
    label.setForeground(options.textColor)
    label.setIcon(options.icon)
    label.setFont(label.getFont.deriveFont(if (message.highPriority) font.getStyle | Font.BOLD else font.getStyle & ~Font.BOLD))
  }
}

