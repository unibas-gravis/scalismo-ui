package scalismo.ui.resources.thirdparty.interpreterpane

import scalismo.ui.resources.thirdparty.ThirdPartyResource

object InterpreterPane extends ThirdPartyResource {
  override def authors: String = "Hanns Holger Rutz"

  override def licenseName: String = "LGPL 2.1"

  override def name: String = "ScalaInterpreterPane"

  override def homepage: Option[String] = Some("https://github.com/Sciss/ScalaInterpreterPane")
}
