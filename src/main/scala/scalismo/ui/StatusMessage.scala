package scalismo.ui

import java.util.Date

import scalismo.ui.StatusMessage.{ Information, Kind }

object StatusMessage {

  sealed trait Kind

  case object Information extends Kind

  case object Warning extends Kind

  case object Error extends Kind

  case object Question extends Kind

}

case class StatusMessage(text: String, kind: Kind = Information, highPriority: Boolean = false, log: Boolean = true) {
  val date = new Date()
}

object Status {
  def set(message: StatusMessage): Unit = StatusImplementation.instance.foreach(_.set(message))

  // convenience method
  def set(text: String): Unit = set(StatusMessage(text))

  def clear(): Unit = StatusImplementation.instance.foreach(_.clear())
}

private[ui] object StatusImplementation {
  private[ui] var instance: Option[StatusImplementation] = None
}

private[ui] trait StatusImplementation {
  def set(message: StatusMessage): Unit

  def clear(): Unit
}