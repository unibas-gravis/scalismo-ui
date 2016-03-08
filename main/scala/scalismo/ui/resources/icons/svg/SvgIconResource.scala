package scalismo.ui.resources.icons.svg

import scalismo.ui.resources.icons.ScalableIcon
import scalismo.ui.view.swing.SVGIcon

object SvgIconResource {
  def load(name: String): SVGIcon with ScalableIcon = {
    val resource = getClass.getResource(name)
    new SVGIcon(resource.toURI.toString) with ScalableIcon
  }
}
