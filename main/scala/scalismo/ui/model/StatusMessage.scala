package scalismo.ui.model

import java.util.Date

import scalismo.ui.model.StatusMessage.{ Information, Kind }

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