package scalismo.ui

import javax.swing.table.AbstractTableModel

import scala.collection.immutable

/**
 * This trait and object are needed for a few very specific cases, where the "core" classes need to call back for user interaction.
 * E.g. the ShapeModel class may in some circumstances pop up a dialog prompting the user to select a specific model from a file.
 * Care must be taken to set up the correct instance initially. For Swing applications, this is done in the ScalismoLookAndFeel class.
 */

object UiFramework {
  private var _instance: UiFramework = null

  def instance: UiFramework = if (_instance != null) _instance else throw new IllegalStateException("UiFramework instance not set")

  def instance_=(newInstance: UiFramework) = {
    _instance = newInstance
  }

  class SelectionTableModel[T](val data: immutable.Seq[TableRow[T]]) extends AbstractTableModel {
    private lazy val titles: immutable.Seq[String] = data.headOption.map(r => r.columnNames).getOrElse(Nil)

    override def getRowCount = data.size

    override def getColumnCount = titles.length

    override def getColumnName(column: Int) = titles(column)

    override def getValueAt(rowIndex: Int, columnIndex: Int) = data(rowIndex).columnValue(columnIndex)

    private var _selected: immutable.Seq[T] = Nil

    def selected: immutable.Seq[T] = _selected

    def selected_=(seq: immutable.Seq[T]) = {
      _selected = seq
    }
  }

  abstract class TableRow[T](val payload: T) {
    val columnNames: immutable.Seq[String]

    def columnValue(index: Int): Object
  }

}

trait UiFramework {

  import scalismo.ui.UiFramework._

  def selectFromTable[T](model: SelectionTableModel[T], title: String, description: String = "", canMultiSelect: Boolean = false, canCancel: Boolean = true): Unit

}
