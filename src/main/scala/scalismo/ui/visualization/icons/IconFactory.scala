package scalismo.ui.visualization.icons

import java.awt.image._
import java.awt.{ Color, Image, Toolkit }
import javax.swing.ImageIcon

import scalismo.ui._
import scalismo.ui.resources.icons.IconResources
import scalismo.ui.visualization.VisualizableSceneTreeObject
import scalismo.ui.visualization.props.{ HasColor, HasOpacity }

object IconFactory {
  final val MinOpacity = 0.3f
  final val MaxOpacity = 1.0f

  final val AlmostWhite = new Color(254, 254, 254)

  final val WhiteToTransparent = new RGBImageFilter {
    override def filterRGB(x: Int, y: Int, rgb: Int): Int = {
      if (rgb == 0xffffffff) 0 else rgb
    }
  }

  private def colorFor(node: VisualizableSceneTreeObject[_])(implicit scene: Scene): Option[HasColor] = {
    node match {
      case ok: HasColor => Some(ok)
      case _ => None
    }
  }

  private def opacityFor(node: VisualizableSceneTreeObject[_])(implicit scene: Scene): Option[HasOpacity] = {
    node match {
      case ok: HasOpacity => Some(ok)
      case _ => None
    }
  }

  def colorOf(node: VisualizableSceneTreeObject[_])(implicit scene: Scene): Option[Color] = {
    (colorFor(node), opacityFor(node)) match {
      case (Some(color), opacityOpt) =>

        def sanitizedOpacity(opacity: Float): Float = {
          Math.max(MinOpacity, Math.min(MaxOpacity, opacity))
        }

        def sanitizedColor(color: Color): Color = {
          if (color == Color.WHITE) AlmostWhite else color
        }

        val c = sanitizedColor(color.color.value)
        val o = (sanitizedOpacity(opacityOpt.map(_.opacity.value).getOrElse(1.0f)) * 255).toInt
        Some(new Color(c.getRed, c.getGreen, c.getBlue, o))
      case _ => None
    }
  }

  def iconFor(node: VisualizableSceneTreeObject[_])(implicit scene: Scene): Option[ImageIcon] = {
    node match {
      case mesh: MeshView => imageFor(colorOf(node), IconResources.Mesh, whiteIsTransparent = true).map(new ImageIcon(_))
      case pc: PointCloudView => imageFor(colorOf(node), IconResources.PointCloud, whiteIsTransparent = true).map(new ImageIcon(_))
      case lm: Landmark => imageFor(colorOf(node), IconResources.Landmark, whiteIsTransparent = false).map(new ImageIcon(_))
      case _ => None
    }
  }

  def imageFor(colorOption: Option[Color], overlay: BufferedImage, whiteIsTransparent: Boolean): Option[Image] = colorOption.map { color =>
    val combined = new BufferedImage(overlay.getWidth, overlay.getHeight, BufferedImage.TYPE_INT_ARGB)
    val g = combined.getGraphics

    // fill the "canvas"
    g.setColor(Constants.Visualization.PerceivedBackgroundColor)
    g.fillRect(0, 0, combined.getWidth, combined.getHeight)

    g.setColor(color)
    g.fillRect(0, 0, combined.getWidth, combined.getHeight)

    g.drawImage(overlay, 0, 0, null)
    if (whiteIsTransparent) {
      // unfortunately, this leaves some nasty "shining" pixels at the edges
      // when a landmark entry is selected, which looks even worse than the square white border.
      val ip = new FilteredImageSource(combined.getSource, WhiteToTransparent)
      Toolkit.getDefaultToolkit.createImage(ip)
    } else combined

  }

}
