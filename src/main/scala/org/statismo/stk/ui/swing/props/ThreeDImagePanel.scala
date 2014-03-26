package org.statismo.stk.ui.swing.props

import scala.swing.BorderPanel
import scala.swing.BoxPanel
import scala.swing.Orientation

import org.statismo.stk.ui.{Image3D, SceneTreeObject}

class ThreeDImagePanel extends BorderPanel with PropertyPanel {
  val description = "Slice positions"

//  val x = new ThreeDImagePlanePanel
//  val y = new ThreeDImagePlanePanel
//  val z = new ThreeDImagePlanePanel

  val box = new BoxPanel(Orientation.Vertical)
//  box.contents ++= Seq(x, y, z)

  layout(box) = BorderPanel.Position.North

  def setObject(obj: Option[AnyRef]): Boolean = {
//    if (obj.isDefined && obj.get.isInstanceOf[Image3D]) {
//      val img = obj.get.asInstanceOf[Image3D]
//      x.setObject(Some(img.xPlane))
//      y.setObject(Some(img.yPlane))
//      z.setObject(Some(img.zPlane))
//      true
//    } else {
//      false
//    }
    false
  }
}