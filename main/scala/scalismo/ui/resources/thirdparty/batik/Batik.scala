package scalismo.ui.resources.thirdparty.batik

import scalismo.ui.resources.thirdparty.ThirdPartyResource

object Batik extends ThirdPartyResource {
  override def authors: String = "The Apache Software Foundation"

  override def licenseName: String = "Apache 2.0"

  override def name: String = "Batik"

  override def homepage: Option[String] = Some("https://xmlgraphics.apache.org/batik")
}
