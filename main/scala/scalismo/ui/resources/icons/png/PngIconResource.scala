package scalismo.ui.resources.icons.png

import javax.imageio.ImageIO
import javax.swing.ImageIcon

import scalismo.ui.resources.icons.ScalableIcon

object PngIconResource {
  def load(name: String): ImageIcon with ScalableIcon = {
    new ImageIcon(ImageIO.read(this.getClass.getResourceAsStream(name))) with ScalableIcon
  }
}
