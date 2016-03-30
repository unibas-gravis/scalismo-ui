package scalismo.ui.resources.thirdparty.spray

import scalismo.ui.resources.thirdparty.ThirdPartyResource

object Spray extends ThirdPartyResource {
  override def authors: String = "Typesafe, Inc."

  override def licenseName: String = "Apache 2.0"

  override def name: String = "spray"

  override def homepage: Option[String] = Some("http://spray.io")
}
