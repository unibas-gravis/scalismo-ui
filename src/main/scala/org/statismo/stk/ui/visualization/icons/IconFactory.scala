package org.statismo.stk.ui.visualization.icons

import java.awt.image._
import java.awt.{Toolkit, Color, Image}
import javax.swing.ImageIcon

import org.statismo.stk.ui._
import org.statismo.stk.ui.resources.icons.IconResources
import org.statismo.stk.ui.visualization.VisualizableSceneTreeObject
import org.statismo.stk.ui.visualization.props.HasColorAndOpacity

import scala.util.Success

object IconFactory {
  final val MinOpacity = 0.3
  final val MaxOpacity = 1.0

  final val AlmostWhite = new Color(254, 254, 254)

  final val WhiteToTransparent = new RGBImageFilter {
    override def filterRGB(x: Int, y: Int, rgb: Int): Int = {
      if (rgb == 0xffffffff) 0 else rgb
    }
  }


  // this returns (an option to) a reference to the "best" HasColorAndOpacity object for the selected node. However, those
  // props are also mutable, so they should not be used for caching.
  private def colorAndOpacityPropsFor(node: VisualizableSceneTreeObject[_])(implicit scene: Scene): Option[HasColorAndOpacity] = {
    // get a (sorted) list of all (viewport, visualization) tuples where a visualization with color and opacity is defined for this object.
    val suitables = scene.viewports.map(viewport =>
      scene.visualizations.tryGet(node, viewport) match {
        case Success(colorAndOpacity: HasColorAndOpacity) => Some((viewport, colorAndOpacity))
        case _ => None
      }
    ).collect { case Some(tuple) => tuple}

    if (suitables.isEmpty) None
    else {
      // take the first suitable visualization that is visible, or fallback to the overall first one.
      val best = suitables.find { case (viewport, vis) =>
        node.isVisibleIn(viewport)
      }
      best.map(_._2).orElse(Some(suitables.head._2))
    }
  }

  def colorOf(node: VisualizableSceneTreeObject[_])(implicit scene: Scene): Option[Color] = {
    colorAndOpacityPropsFor(node).map { props =>

      def sanitizedOpacity(opacity: Double): Double = {
        Math.max(MinOpacity, Math.min(MaxOpacity, opacity))
      }

      def sanitizedColor(color: Color): Color = {
        if (color == Color.WHITE) AlmostWhite else color
      }

      val c = sanitizedColor(props.color.value)
      val o = (sanitizedOpacity(props.opacity.value) * 255).toInt
      new Color(c.getRed, c.getGreen, c.getBlue, o)
    }
  }

  def iconFor(node: VisualizableSceneTreeObject[_])(implicit scene: Scene): Option[ImageIcon] = {
    node match {
      case mesh: Mesh => imageFor(colorOf(node), IconResources.Mesh, whiteIsTransparent = true).map(new ImageIcon(_))
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
    }
    else combined

  }

}
