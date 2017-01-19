package scalismo.ui.swing.util

import javax.imageio.ImageIO
import javax.swing.ImageIcon

object PngIconResource {
  def load(name: String): ImageIcon = {
    new ImageIcon(ImageIO.read(this.getClass.getResourceAsStream(name)))
  }
}
