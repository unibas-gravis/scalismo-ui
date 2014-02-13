package org.statismo.stk.ui.swing

import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Orientation

import org.statismo.stk.ui.SceneTreeObject
import org.statismo.stk.ui.ThreeDImage

class ThreeDImagePanel extends BorderPanel with SceneObjectPropertyPanel {
  val description = "Slice positions"

  val x = new ThreeDImagePlanePanel
  val y = new ThreeDImagePlanePanel
  val z = new ThreeDImagePlanePanel

  val box = new BoxPanel(Orientation.Vertical)
  box.contents ++= Seq(x, y, z)

  layout(box) = BorderPanel.Position.North

  def setObject(obj: Option[SceneTreeObject]): Boolean = {
    if (obj.isDefined && obj.get.isInstanceOf[ThreeDImage]) {
      val img = obj.get.asInstanceOf[ThreeDImage]
      x.setObject(Some(img.xPlane))
      y.setObject(Some(img.yPlane))
      z.setObject(Some(img.zPlane))
      true
    } else {
      false
    }
  }
}