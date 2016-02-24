package scalismo.ui.toolkit

object Toolkit {
  private var _instance: Toolkit = null

  def instance: Toolkit = if (_instance != null) _instance else throw new IllegalStateException("Toolkit instance not set")

  def instance_=(newInstance: Toolkit) = {
    _instance = newInstance
  }

//  class SelectionTableModel[T](val data: immutable.Seq[TableRow[T]]) extends AbstractTableModel {
//    private lazy val titles: immutable.Seq[String] = data.headOption.map(r => r.columnNames).getOrElse(Nil)
//
//    override def getRowCount = data.size
//
//    override def getColumnCount = titles.length
//
//    override def getColumnName(column: Int) = titles(column)
//
//    override def getValueAt(rowIndex: Int, columnIndex: Int) = data(rowIndex).columnValue(columnIndex)
//
//    private var _selected: immutable.Seq[T] = Nil
//
//    def selected: immutable.Seq[T] = _selected
//
//    def selected_=(seq: immutable.Seq[T]) = {
//      _selected = seq
//    }
//  }
//
//  abstract class TableRow[T](val payload: T) {
//    val columnNames: immutable.Seq[String]
//
//    def columnValue(index: Int): Object
//  }
//
}

trait Toolkit {

  //def selectFromTable[T](model: SelectionTableModel[T], title: String, description: String = "", canMultiSelect: Boolean = false, canCancel: Boolean = true): Unit

}
