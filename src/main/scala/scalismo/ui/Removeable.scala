package scalismo.ui

import scala.swing.event.Event

object Removeable {

  case class Removed(source: Removeable) extends Event

}

trait Removeable extends EdtPublisher {
  def remove() = {
    publishEdt(Removeable.Removed(this))
  }

  protected[ui] def isCurrentlyRemoveable = true
}

trait RemoveableChildren {
  protected[ui] def children: Seq[Removeable]

  def removeAll(): Unit
}

trait RemoveableWithChildren extends Removeable with RemoveableChildren {
  override def remove() = {
    removeAll()
    super.remove()
  }

  override def isCurrentlyRemoveable = {
    children.foldLeft(false)({
      case (b, c) => b || c.isCurrentlyRemoveable
    })
  }
}
