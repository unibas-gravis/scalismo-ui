package org.statismo.stk.ui.swing

import org.statismo.stk.ui.SceneTreeObject
import org.statismo.stk.ui.Colorable
import scala.swing.Slider
import scala.swing.BorderPanel
import scala.swing.event.ValueChanged
import scala.swing.Component
import javax.swing.JColorChooser
import scala.swing.Swing
import java.awt.Color
import scala.swing.event.Event
import javax.swing.colorchooser.DefaultSwatchChooserPanel
import org.statismo.stk.ui.swing.util.ColorPickerPanel
import javax.swing.border.TitledBorder
import scala.swing.Label
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JPanel
import java.awt.Graphics
import javax.swing.event.ChangeListener
import javax.swing.event.ChangeEvent
import org.statismo.stk.ui.ThreeDImagePlane
import org.statismo.stk.ui.ThreeDImage
import scala.swing.BoxPanel
import scala.swing.Orientation

class ThreeDImagePanel extends BorderPanel with SceneObjectPropertyPanel {
  val description = "Slice positions"
    
  val x = new ThreeDImagePlanePanel
  val y = new ThreeDImagePlanePanel
  val z = new ThreeDImagePlanePanel
  
  val box = new BoxPanel(Orientation.Vertical)
  box.contents ++= Seq(x,y,z)
  
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