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

package scalismo.ui.view.action.popup

import java.io.File
import javax.swing.text.TableView

import scalismo.io.StatismoIO
import scalismo.io.StatismoIO.{ CatalogEntry, StatismoModelType }
import scalismo.ui.model.{ GroupNode, SceneNode }
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.util.{ FileIoMetadata, FileUtil }
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.action.LoadAction

import scala.swing.BorderPanel.Position
import scala.swing.{ Action, Alignment, BorderPanel, BoxPanel, Button, ComboBox, Dialog, FlowPanel, GridPanel, Label, Orientation, Swing, Table, TextArea, TextField }
import scala.util.{ Failure, Success, Try }

object LoadStatisticalShapeModelAction extends PopupAction.Factory {
  override def apply(context: List[SceneNode])(implicit frame: ScalismoFrame): List[PopupAction] = {
    singleMatch[GroupNode](context).map(n => new LoadStatisticalShapeModelAction(n)).toList
  }
}

class LoadStatisticalShapeModelAction(group: GroupNode)(implicit frame: ScalismoFrame)
    extends PopupAction(s"Load ${FileIoMetadata.StatisticalShapeModel.description} ...", BundledIcon.Load) {

  def load(file: File): Try[Unit] = {

    for {
      path <- selectPathFromFile(file)
      model <- StatismoIO.readStatismoMeshModel(file, path)
    } yield {
      val basename = FileUtil.basename(file)
      group.addStatisticalMeshModel(model, basename)
    }
  }

  override def apply(): Unit = {
    new LoadAction(load, FileIoMetadata.StatisticalShapeModel).apply()
  }

  private def selectPathFromFile(file: File): Try[String] = {

    StatismoIO.readModelCatalog(file) match {

      case Failure(ex) =>
        if (ex == StatismoIO.NoCatalogPresentException) {
          // no catalog, assuming a single contained model
          Success("/")
        } else Failure(ex)

      case Success(catalog) =>
        val entries = catalog.filter(e => e.modelType == StatismoModelType.Polygon_Mesh)

        if (entries.isEmpty) {
          Failure(new IllegalArgumentException("File does not contain any usable model"))
        } else if (entries.length == 1) {
          Success(entries.head.modelPath)
        } else {
          val title = "Select shape model to load"
          val description = "The file contains more that one shape model. Please select the one you wish to load."

          var items = List("Item 1", "Item 2", "Item 3", "Item 4")

          val dialog = new ShapeModelSelectionDialog(entries)
          dialog.open()

          Success(dialog.path)
        }
    }
  }

  // shows a combobox with the catalog entries
  private class ShapeModelSelectionDialog(entries: Seq[CatalogEntry]) extends Dialog(frame) {

    private val text1 = new Label("Several models are stored in the h5 file.")
    text1.horizontalAlignment = Alignment.Left
    private val text2 = new Label("Please select which one you would like to load.")
    text2.horizontalAlignment = Alignment.Left

    private val combo = new ComboBox(entries.map(_.name))

    // centers dialog on frame
    this.peer.setLocationRelativeTo(null)

    title = "Select model"
    modal = true

    contents = new BoxPanel(Orientation.Vertical) {
      contents += new GridPanel(3, 1) {
        contents += text1
        contents += text2
        contents += combo
      }
      contents += new FlowPanel() {
        contents += new Button(Action("ok") {
          close()
        })
      }
    }

    pack()

    // the path entry will always be found, as the user can only select entries from the
    // same list. Hence .get is justified here.
    def path = entries.find(_.name == combo.selection.item).get.modelPath

  }

}