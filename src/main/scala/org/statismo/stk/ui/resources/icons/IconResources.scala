package org.statismo.stk.ui.resources.icons

import javax.imageio.ImageIO

object IconResources {
  final lazy val Landmark = ImageIO.read(this.getClass.getResourceAsStream("landmark.png"))
  final lazy val Mesh = ImageIO.read(this.getClass.getResourceAsStream("mesh.png"))
}