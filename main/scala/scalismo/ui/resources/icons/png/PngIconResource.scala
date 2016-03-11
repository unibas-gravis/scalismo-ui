package scalismo.ui.resources.icons.png

import javax.imageio.ImageIO
import javax.swing.{ Icon, ImageIcon }

import scalismo.ui.resources.icons.ScalableIcon
import scalismo.ui.view.util.ScalableUI

object PngIconResource {
  def load(name: String): ImageIcon with ScalableIcon = {
    new ImageIcon(ImageIO.read(this.getClass.getResourceAsStream(name))) with ScalableIcon {
      override def resize(width: Int, height: Int): Icon = ScalableUI.resizeIcon(this, width, height)
    }
  }
}
