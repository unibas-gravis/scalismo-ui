package scalismo.ui.swing

import java.awt.event.{ MouseAdapter, MouseEvent }
import javax.swing.event.{ ListSelectionEvent, ListSelectionListener }
import javax.swing.{ BorderFactory, JScrollPane, JTable, ListSelectionModel }

import scalismo.ui.UiFramework
import scalismo.ui.UiFramework.SelectionTableModel

import scala.swing._

class SwingUiFramework extends UiFramework {
  override def selectFromTable[T](model: SelectionTableModel[T], title: String, description: String, canMultiSelect: Boolean, canCancel: Boolean) = {
    val dialog = new Dialog {
      modal = true
    }
    dialog.title = title

    val panel = new BorderPanel

    if (description != null && description.trim().length > 0) {
      val label = new Label(description)
      label.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
      panel.layout(label) = BorderPanel.Position.North
    }

    val ok = new Action("OK") {
      enabled = false

      override def apply() = dialog.dispose()
    }

    val cancel = new Action("Cancel") {
      override def apply() = {
        model.selected = Nil
        dialog.dispose()
      }
    }

    val buttons = new GridPanel(1, if (canCancel) 2 else 1)
    buttons.contents ++= Seq(new Button(ok))
    if (canCancel) {
      buttons.contents ++= Seq(new Button(cancel))
    }

    panel.layout(new BorderPanel {
      layout(buttons) = BorderPanel.Position.East
    }) = BorderPanel.Position.South

    val table = new JTable(model) {
      setFillsViewportHeight(true)
      setSelectionMode(if (canMultiSelect) ListSelectionModel.MULTIPLE_INTERVAL_SELECTION else ListSelectionModel.SINGLE_SELECTION)
    }

    table.getSelectionModel.addListSelectionListener(new ListSelectionListener {
      override def valueChanged(e: ListSelectionEvent) = {
        if (!e.getValueIsAdjusting) {
          val indexes = table.getSelectedRows.deep
          model.selected = model.data.zipWithIndex.filter {
            case (r, i) => indexes.contains(i)
          }.map {
            case (r, i) => r.payload
          }
          ok.enabled = model.selected.nonEmpty
        }
      }
    })

    table.addMouseListener(new MouseAdapter() {
      override def mouseClicked(e: MouseEvent) = {
        if (e.getClickCount == 2) {
          ok.apply()
        }
      }
    })

    val scroll = new JScrollPane(table)

    panel.layout(Component.wrap(scroll)) = BorderPanel.Position.Center

    dialog.contents = panel
    dialog.pack()
    dialog.centerOnScreen()
    dialog.visible = true
  }
}
